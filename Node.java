import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.HashSet;
import org.json.*; 

public class Node {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private String name;
    private BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(10);
    // Proposer variables
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

            // LISTENING THREAD FOR MESSAGES
            Thread messageListener = new Thread(() -> {
                try {
                    String receivedMessage;
                    while ((receivedMessage = in.readLine()) != null) {
                        if (receivedMessage.startsWith("NODES:")) {
                            updateOtherNodeNames(receivedMessage);
                        } else if (receivedMessage.startsWith(this.name)) {
                            try {
                                messageQueue.put(receivedMessage);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageListener.start();

            // USER INPUTS DEBUGGING
            Thread inputListener = new Thread(() -> {
                try {
                    String input;
                    while ((input = userInput.readLine()) != null) {
                        out.println(input);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            inputListener.start();

            // HANDLE RECEIVED MESSAGES
            while (true) {
                String receivedMessage = messageQueue.poll(); 
                if (receivedMessage != null) {
                    // PHASE 1
                    if (receivedMessage.contains("PREPARE")) {
                        handlePrepareMessage(receivedMessage, out);
                    }
                    else if (receivedMessage.contains("PROMISE") && this.proposer) {
                        handlePromiseMessage(receivedMessage, out);
                    }
                }
            }
        } catch (IOException e) {
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

    private void handlePromiseMessage(String promiseMessage, PrintWriter out) {
        // TODO: Implement this
        System.out.println("Received promise message: " + promiseMessage);
    }

    /* PHASE 2 */
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
