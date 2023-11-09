import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.HashSet;
import org.json.*; 
import java.util.Random; 

public class Node {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private String name;
    private BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(10);
    private Thread messageListener; 
    // Proposer variables
    private Thread proposerThread; 
    private BlockingQueue<String> promiseQueue = new ArrayBlockingQueue<>(10);
    private BlockingQueue<String> acceptQueue = new ArrayBlockingQueue<>(10);
    private Boolean proposer;
    private Boolean hasWaited = false;
    private HashSet<String> otherNodeNames = new HashSet<>();
    private int connectedNodeCount;
    private ProposalNumber proposalNumber;

    public Node(String name, Boolean proposer) {
        this.name = name;
        this.proposer = proposer;
        if (proposer) {
            proposalNumber = new ProposalNumber(name, 1);
        }

        // Create / update JSON file
        if (!JSONUtils.jsonFileExists(this.name + ".json")) {
            JSONObject json = new JSONObject();
            json.put("name", this.name);
            json.put("proposer", this.proposer);
            json.put("proposal_accepted", false);
            json.put("accepted_id", 0); 
            json.put("accepted_value", ""); 
            json.put("max_id", 0);
            JSONUtils.createJSONFile(this.name + ".json", json);
        }
        else {
            System.out.println("JSON file detected!");
        }
    }

    public void connect() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println(name + " successfully connected to the server.");
            out.println(name + ":Connected");

            if (this.proposer) {
                startProposerThread(out); 
            }

            // LISTENING THREAD FOR MESSAGES
            messageListener = new Thread(() -> {
                try {
                    String receivedMessage;
                    while (!Thread.interrupted()) {
                        if (in.ready()) {
                            receivedMessage = in.readLine();
                            if (receivedMessage == null) break;
                            if (receivedMessage.startsWith("NODES:")) {
                                updateOtherNodeNames(receivedMessage);
                            } else if (receivedMessage.startsWith(this.name)) {
                                // If proposer and received promise message, add to promiseQueue
                                if (this.proposer && receivedMessage.contains("PROMISE")) {
                                    try {
                                        promiseQueue.put(receivedMessage);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                // If proposer and received accept message, add to acceptQueue
                                } else if (this.proposer && receivedMessage.contains("ACCEPT")) {
                                    try {
                                        // System.out.println("RECEIVED: " + receivedMessage);
                                        acceptQueue.put(receivedMessage);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                // If acceptor, add to messageQueue
                                } else {
                                    try {
                                        messageQueue.put(receivedMessage);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }                                
                                }
                            }        
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                break; // Exit the loop if interrupted during sleep
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageListener.start();
            

            // ACCEPTOR -> HANDLE RECEIVED MESSAGES
            while (true) {
                String receivedMessage = messageQueue.poll(); 
                if (receivedMessage != null) {
                    // PHASE 1
                    if (receivedMessage.contains("PREPARE")) {
                        handlePrepareMessage(receivedMessage, out);
                    } else if (receivedMessage.contains("PROPOSE")) {
                        handleProposeMessage(receivedMessage, out);
                    } else if (receivedMessage.contains("DECIDE")) {
                        handleDecideMessage(receivedMessage, out); 
                        System.exit(0);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startProposerThread(PrintWriter out) {
        proposerThread = new Thread(() -> proposerLogic(out));
        proposerThread.start();
    }

    // Proposer logic with PrintWriter
    private void proposerLogic(PrintWriter out) {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Wait for 10 seconds before sending prepare message
                proposalNumber.increment();
                Thread.sleep(10000);
    
                // Send prepare messages to all other nodes
                for (String node : otherNodeNames) {
                    String prepareMessage = MessageConstructor.makePrepare(node, this.name, "PREPARE", proposalNumber.getProposalNumber());
                    out.println(prepareMessage);
                    printWithTimestamp("SENT: " + prepareMessage);
                }
    
                if (Thread.interrupted()) {
                    break;
                }
                // Wait for 5 seconds to process responses
                printWithTimestamp("> Waiting for promises...");
                long startTime = System.currentTimeMillis();
                long currentTime = startTime;
                boolean majorityPromisesReceived = false;
    
                while (currentTime - startTime < 5000) {
                    if (promiseQueue.size() > connectedNodeCount / 2) {
                        majorityPromisesReceived = true;
                        break;
                    }
    
                    Thread.sleep(100); // Check every 100 milliseconds
                    currentTime = System.currentTimeMillis();
                }
    
                if (!majorityPromisesReceived) {
                    printWithTimestamp("Did not receive majority of promises");
                    continue;
                }
    
                // Do any responses contain a value?
                Float highestProposalNumber = 0f;
                String highestProposalValue = "";
                Boolean valueExists = false;
                for (String promise : promiseQueue) {
                    String[] promiseSplit = promise.split(":");
                    if (promiseSplit.length > 5) {
                        valueExists = true;
                        if (Float.parseFloat(promiseSplit[4]) > highestProposalNumber) {
                            highestProposalNumber = Float.parseFloat(promiseSplit[4]);
                            highestProposalValue = promiseSplit[5];
                        }
                    }
                }
    
                // If value exists, send propose message to all other nodes
                String chosenValue = "";
                if (!valueExists) {
                    chosenValue = chooseValue();
                } else {
                    chosenValue = highestProposalValue;
                }
    
                for (String node : otherNodeNames) {
                    String proposeMessage = MessageConstructor.makePropose(node, this.name, "PROPOSE", proposalNumber.getProposalNumber(), chosenValue);
                    out.println(proposeMessage);
                    printWithTimestamp("SENT: " + proposeMessage);
                }
    
                if (Thread.interrupted()) {
                    break;
                }
                // Wait for 5 seconds to wait for accept messages
                printWithTimestamp("> Waiting for accept messages...");
                startTime = System.currentTimeMillis();
                currentTime = startTime;
                boolean majorityAcceptsReceived = false;
    
                while (currentTime - startTime < 5000) {
                    if (acceptQueue.size() > connectedNodeCount / 2) {
                        majorityAcceptsReceived = true;
                        break;
                    }
    
                    Thread.sleep(100); // Check every 100 milliseconds
                    currentTime = System.currentTimeMillis();
                }
    
                if (!majorityAcceptsReceived) {
                    printWithTimestamp("> Did not receive majority of accept messages");
                    printWithTimestamp("> Received " + acceptQueue.size() + " accept messages out of " + connectedNodeCount);
                    continue;
                }

                printWithTimestamp("> Majority of accept messages received!");
                printWithTimestamp("> Received " + acceptQueue.size() + " accept messages out of " + connectedNodeCount);

                // Send decide message to all other nodes
                for (String node : otherNodeNames) {
                    String decideMessage = MessageConstructor.makeDecide(node, this.name, chosenValue);
                    out.println(decideMessage);
                    printWithTimestamp("SENT:" + decideMessage);
                }
    
                // Exit the program
                stopThreadsAndExit();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            e.printStackTrace();
        }
    }
    

    private void updateOtherNodeNames(String receivedMessage) {
        String[] names = receivedMessage.split(":");
        for (int i = 1; i < names.length; i++) {
            otherNodeNames.add(names[i]);
        }
        if (otherNodeNames.contains(this.name)) {
            otherNodeNames.remove(this.name);
        }
        connectedNodeCount = otherNodeNames.size() + 1;
    }

    /* PHASE 1 */
    private void handlePrepareMessage(String prepareMessage, PrintWriter out) {
        printWithTimestamp("RECEIVED " + prepareMessage);

        JSONObject json = JSONUtils.readJSONFile(this.name + ".json");
        String[] prepareMessageSplit = prepareMessage.split(":");
        String from = prepareMessageSplit[1];
        String proposalNumber = prepareMessageSplit[3];

        if (Float.parseFloat(proposalNumber) < json.getFloat("max_id")) {
            System.out.println("Proposal number is less than max_id");
        } else {
            json.put("max_id", Float.parseFloat(proposalNumber));
            if (json.getBoolean("proposal_accepted")) {
                System.out.println("Proposal has been accepted");
                String promiseMessage = MessageConstructor.makePromise(from, this.name, "PROMISE", proposalNumber, Float.toString(json.getFloat("accepted_id")), json.getString("accepted_value"));
                out.println(promiseMessage);
                printWithTimestamp("SENT:" + promiseMessage);
            } else {
                String promiseMessage = MessageConstructor.makePromise(from, this.name, "PROMISE", proposalNumber);
                out.println(promiseMessage);
                printWithTimestamp("SENT:" + promiseMessage);
            }
        }

        JSONUtils.updateJSONFile(this.name + ".json", json);
    }

    /* PHASE 2 */
    void handleProposeMessage(String proposeMessage, PrintWriter out) {
        printWithTimestamp("RECEIVED:" + proposeMessage);

        JSONObject json = JSONUtils.readJSONFile(this.name + ".json");
        String[] proposeMessageSplit = proposeMessage.split(":");
        String from = proposeMessageSplit[1];
        String proposalNumber = proposeMessageSplit[3];
        String value = proposeMessageSplit[4];

        if (Float.parseFloat(proposalNumber) < json.getFloat("max_id")) {
            System.out.println("Proposal number is less than max_id");
        } else {
            json.put("max_id", Float.parseFloat(proposalNumber));
            json.put("proposal_accepted", true);
            json.put("accepted_id", Float.parseFloat(proposalNumber));
            json.put("accepted_value", value);

            String acceptMessage = MessageConstructor.makeAccept(from, this.name, "ACCEPT", proposalNumber, value);
            out.println(acceptMessage);
            printWithTimestamp("SENT: " + acceptMessage);
            // System.out.println("RECEIVED: " + proposalNumber);
            // System.out.println("max_id: " + json.getFloat("max_id"));
        }

        JSONUtils.updateJSONFile(this.name + ".json", json);
    }

    String chooseValue() {
        return this.name; 
    }

    /* PHASE 3 */
    // DECIDE:VALUE
    void handleDecideMessage(String decideMessage, PrintWriter out) {
        printWithTimestamp("RECEIVED: " + decideMessage);

        JSONObject json = JSONUtils.readJSONFile(this.name + ".json");
        json.put("proposal_accepted", true);
        json.put("accepted_id", Float.parseFloat(proposalNumber.getProposalNumber()));
        json.put("accepted_value", chooseValue());

        JSONUtils.updateJSONFile(this.name + ".json", json);
        String[] decideMessageSplit = decideMessage.split(":");
        System.out.println("DECIDED: " + decideMessageSplit[3]); 
    }

    // Method to stop all threads and exit the program
    private void stopThreadsAndExit() {
        // Interrupt the proposer thread
        if (proposerThread != null) {
            proposerThread.interrupt();
        }

        // Stop the message listener thread
        if (messageListener != null) {
            messageListener.interrupt();
        }

        // Exit the program
        System.exit(0);
    }

// Print with timestamp in "hh:mm:ss:xx" format
private void printWithTimestamp(String message) {
    long currentTimeMillis = System.currentTimeMillis();
    long currentTimeSeconds = currentTimeMillis / 1000;
    long seconds = currentTimeSeconds % 60;
    long minutes = (currentTimeSeconds / 60) % 60;
    long millis = currentTimeMillis % 1000;

    String timestamp = String.format("%02d:%02d:%03d", minutes, seconds, millis);
    System.out.println("[" + timestamp + "] " + message);
}

    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide a name for the council member.");
            return;
        }

        Boolean isProposer = false;
        if (args[1].toLowerCase().equals("t")) {
            isProposer = true;
        } 

        Node member = new Node(args[0], isProposer);
        member.connect();
    }
}
