import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.HashSet;


public class Node {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private String name;
    private Boolean proposer;
    private BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(10);
    private HashSet<String> otherNodeNames = new HashSet<>();
    private int connectedNodeCount;

    public Node(String name, Boolean proposer) {
        this.name = name;
        this.proposer = proposer;
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
                    // Process received message and send response if needed
                    System.out.println("Received message: " + receivedMessage);
                    // print otherNodeNames
                    System.out.println("Other node names: " + otherNodeNames);
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

    // Proposer functions 
    // Acceptor functions
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide a name for the council member.");
            return;
        }

        Boolean isProposer = false;
        if (args[1].toLowerCase() == "true" ) {
            isProposer = true;
        }

        Node member = new Node(args[0], isProposer);
        member.connect();
    }
}
