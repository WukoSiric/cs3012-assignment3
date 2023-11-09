public class MessageConstructor {
    // MESSAGE STRUCTURE TO:FROM:TYPE:PROPOSALNUMBER:VALUE 

    static public String makePrepare(String to, String from, String type, String proposalNumber) {
        return to + ":" + from + ":" + type + ":" + proposalNumber;
    }

    static public String makePromise(String to, String from, String type, String proposalNumber, String acceptedID, String value) {
        return to + ":" + from + ":" + type + ":" + proposalNumber + ":" + acceptedID  + ":" + value;
    }

    static public String makePromise(String to, String from, String type, String proposalNumber) {
        return to + ":" + from + ":" + type + ":" + proposalNumber;
    }

    static public String makePropose(String to, String from, String type, String proposalNumber, String value) {
        return to + ":" + from + ":" + type + ":" + proposalNumber + ":" + value;
    }

    

}