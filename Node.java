import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Node {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private String name;
    private Boolean proposer;
    private BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(10);

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

            // Listening thread to print and store incoming messages
            Thread messageListener = new Thread(() -> {
                try {
                    String receivedMessage;
                    while ((receivedMessage = in.readLine()) != null) {
                        System.out.println(receivedMessage);
                        try {
                            messageQueue.put(receivedMessage);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageListener.start();

            // Thread to listen to inputs from the user (debugging)
            Thread inputListener = new Thread(() -> {
                try {
                    String input;
                    while ((input = userInput.readLine()) != null) {
                        out.println(name + ":" + input);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            inputListener.start();

            // Sending responses to received messages
            String response;
            while (true) {
                String receivedMessage = messageQueue.poll(); 
                if (receivedMessage != null) {
                    // Process received message and send response if needed
                    System.out.println("Received message: " + receivedMessage);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
