public class MessageConstructor {
    MessageType type; // Enum: PREPARE, PROMISE, ACCEPT
    
    enum MessageType {
        PREPARE, PROMISE, ACCEPT, ACCEPT_REQUEST 
    }
}