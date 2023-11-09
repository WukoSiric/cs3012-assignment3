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
    // Proposer variables
    private BlockingQueue<String> promiseQueue = new ArrayBlockingQueue<>(10);
    private BlockingQueue<String> acceptQueue = new ArrayBlockingQueue<>(10);
    private Boolean proposer;
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
            Thread messageListener = new Thread(() -> {
                try {
                    String receivedMessage;
                    while ((receivedMessage = in.readLine()) != null) {
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
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startProposerThread(PrintWriter out) {
        Thread proposerThread = new Thread(() -> proposerLogic(out));
        proposerThread.start();
    }

    // Proposer logic with PrintWriter
    private void proposerLogic(PrintWriter out) {
        try {
            while (true) {
                // Wait for 10 seconds before sending prepare message
                Thread.sleep(10000);

                // Send prepare messages to all other nodes
                for (String node : otherNodeNames) {
                    String prepareMessage = MessageConstructor.makePrepare(node, this.name, "PREPARE", proposalNumber.getProposalNumber());
                    out.println(prepareMessage);
                    System.out.println("Sent prepare message to " + node + " ->" + prepareMessage);
                }
                proposalNumber.increment();

                // Wait for 7 seconds to process responses
                Thread.sleep(7000);
                
                // Check if majority of nodes have promised
                ArrayList<String> promises = new ArrayList<>();
                while (promiseQueue.size() > 0) {
                    promises.add(promiseQueue.take());
                }

                if (!((promises.size() + 1) > connectedNodeCount / 2)) { // promises.size() + 1 because proposer is also a node
                    continue; 
                } 

                // Do any responses contain a value?
                Float highestProposalNumber = 0f; 
                String highestProposalValue = "";
                Boolean valueExists = false;
                for (String promise : promises) {
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
                String proposeMessage = "";
                if (valueExists) {
                    for (String node : otherNodeNames) {
                        proposeMessage = MessageConstructor.makePropose(node, this.name, "PROPOSE", proposalNumber.getProposalNumber(), highestProposalValue);
                        out.println(proposeMessage);
                        System.out.println("Sent propose message to " + node + " ->" + proposeMessage);
                    }
                } else {
                    String chosenValue = chooseValue();
                    for (String node : otherNodeNames) {
                        proposeMessage = MessageConstructor.makePropose(node, this.name, "PROPOSE", proposalNumber.getProposalNumber(), chosenValue);
                        out.println(proposeMessage);
                        System.out.println("Sent propose message to " + node + " ->" + proposeMessage);
                    }
                }

                // Wait 7 seconds to wait for accept messages
                Thread.sleep(7000);
                
                // Check if received majority of accept messages
                ArrayList<String> accepts = new ArrayList<>();
                while (acceptQueue.size() > 0) {
                    accepts.add(acceptQueue.take());
                }

                if (!((accepts.size() + 1) > connectedNodeCount / 2)) { // accepts.size() + 1 because proposer is also a node
                    System.out.println("Did not receive majority of accept messages");
                    System.out.println("Received " + accepts.size() + " accept messages");
                    continue; 
                }

                // Send decide message to all other nodes
                for (String node : otherNodeNames) {
                    String decideMessage = MessageConstructor.makeDecide(node, this.name);
                    out.println(node + ":" + decideMessage);
                    System.out.println("Sent decide message to " + node + " ->" + decideMessage);
                }
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
        System.out.println("Received prepare message: " + prepareMessage);

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
                System.out.println("Sent promise message: " + promiseMessage);
            } else {
                String promiseMessage = MessageConstructor.makePromise(from, this.name, "PROMISE", proposalNumber);
                out.println(promiseMessage);
                System.out.println("Sent promise message: " + promiseMessage);
            }
        }

        JSONUtils.updateJSONFile(this.name + ".json", json);
    }

    /* PHASE 2 */
    void handleProposeMessage(String proposeMessage, PrintWriter out) {
        System.out.println("Received propose message: " + proposeMessage);

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
            System.out.println("Sent accept message: " + acceptMessage);
        }

        JSONUtils.updateJSONFile(this.name + ".json", json);
    }

    String chooseValue() {
        String[] foods = {
            "Pizza", "Burger", "Pasta", "Sushi", "Salad",
            "Sandwich", "Steak", "Soup", "Tacos", "Burrito",
            "Curry", "Risotto", "Paella", "Falafel", "Lasagna",
            "Dumplings", "Quiche", "Gnocchi", "Ramen", "Pancakes"
        };

        Random random = new Random();
        int index = random.nextInt(foods.length); // Generates a random index between 0 and (foods.length - 1)
        
        return foods[index];
    }

    /* PHASE 3 */
    // DECIDE:VALUE
    void handleDecideMessage(String decideMessage, PrintWriter out) {
        System.out.println("Received decide message: " + decideMessage);

        JSONObject json = JSONUtils.readJSONFile(this.name + ".json");
        json.put("proposal_accepted", true);
        json.put("accepted_id", Float.parseFloat(proposalNumber.getProposalNumber()));
        json.put("accepted_value", chooseValue());

        JSONUtils.updateJSONFile(this.name + ".json", json);

        System.out.println("Decided on value: " + json.getString("accepted_value"));
        System.exit(0);
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
