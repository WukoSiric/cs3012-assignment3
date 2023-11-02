import java.io.*;
import java.net.*;

public class CouncilMember {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private String name;

    public CouncilMember(String name) {
        this.name = name;
    }

    public void connect() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
             
            System.out.println(name + " connected to the server.");

            // Send a message after connecting
            out.println(name + ": Hello everyone!");

            // Start a thread to listen for messages from other council members
            Thread messageListener = new Thread(() -> {
                try {
                    String receivedMessage;
                    while ((receivedMessage = in.readLine()) != null) {
                        System.out.println(receivedMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageListener.start();

            // Main thread will handle user input and sending messages
            String messageToSend;
            while (true) {
                messageToSend = userInput.readLine();
                if (messageToSend.equalsIgnoreCase("exit")) {
                    break; // Exit the loop and disconnect
                }
                out.println(name + ": " + messageToSend);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a name for the council member.");
            return;
        }

        CouncilMember member = new CouncilMember(args[0]);
        member.connect();
    }
}
