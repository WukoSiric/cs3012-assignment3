public class Message {
    MessageType type; // Enum: PREPARE, PROMISE, ACCEPT
    int prepareID; 

    enum MessageType {
        PREPARE, PROMISE, ACCEPT, ACCEPT_REQUEST 
    }
}