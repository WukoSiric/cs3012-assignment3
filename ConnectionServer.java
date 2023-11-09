import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionServer {
    private static final int PORT = 12345;
    private static List<ClientHandler> connectedClients = Collections.synchronizedList(new ArrayList<>());
    private static List<String> connectedNodeNames = Collections.synchronizedList(new ArrayList<>()); 

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Connection Server started on port: " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket);
                connectedClients.add(client);
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class to handle client messages
    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        
        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Update connected node names
                    if (inputLine.endsWith(":Connected")) {
                        String nodeName = inputLine.substring(0, inputLine.indexOf(":Connected"));
                        connectedNodeNames.add(nodeName);
                        broadcastConnectedNodeNames();
                    } else {
                        broadcast(inputLine);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public void sendMessage(String msg) {
            out.println(msg);
        }

        private void broadcast(String msg) {
            for (ClientHandler client : connectedClients) {
                if (client != this) { // Don't send back to the sender
                    client.sendMessage(msg);
                }
            }
        }

        private static void broadcastConnectedNodeNames() {
            StringBuilder names = new StringBuilder();
            names.append("NODES:");
            for (String nodeName : connectedNodeNames) {
                names.append(nodeName).append(":");
            }
            
            // Remove the trailing colon, if any
            if (names.length() > 0) {
                names.deleteCharAt(names.length() - 1);
            }
            String message = names.toString();
            for (ClientHandler client : connectedClients) {
                client.sendMessage(message);
            }
        }
        
    }
}
