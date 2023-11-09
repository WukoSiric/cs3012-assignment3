public class MessageConstructor {
    MessageType type; // Enum: PREPARE, PROMISE, ACCEPT
    // MESSAGE STRUCTURE TO:FROM:TYPE:PROPOSALNUMBER:VALUE 

    enum MessageType {
        PREPARE, PROMISE, ACCEPT, ACCEPT_REQUEST 
    }

    public String makePrepare(String to, String from, String proposalNumber) {
        this.type = MessageType.PREPARE;
        return to + ":" + from + ":" + type + ":" + proposalNumber;
    }

    public String makePromise(String to, String from, String proposalNumber, String value) {
        this.type = MessageType.PROMISE;
        return to + ":" + from + ":" + type + ":" + proposalNumber + ":" + value;
    }

    public String makePromise(String to, String from, String proposalNumber) {
        this.type = MessageType.PROMISE;
        return to + ":" + from + ":" + type + ":" + proposalNumber;
    }

    public String makePropose(String to, String from, String proposalNumber, String value) {
        this.type = MessageType.ACCEPT;
        return to + ":" + from + ":" + type + ":" + proposalNumber + ":" + value;
    }

    

}