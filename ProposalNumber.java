public class ProposalNumber {
    private String proposalNumber;
    private String nodeName; //M1
    private int roundNumber;

    public ProposalNumber(String nodeName, int roundNumber) {
        this.nodeName = nodeName;
        this.roundNumber = roundNumber;

        this.proposalNumber = roundNumber + "." + nodeName.substring(1);
    }

    public void increment() {
        this.roundNumber++;
        this.proposalNumber = roundNumber + "." + nodeName.substring(1);
    }

    public String getProposalNumber() {
        return proposalNumber;
    }

    public Boolean isGreaterThanCurrent(String other) {
        String[] otherSplit = other.split("\\.");
        int otherRoundNumber = Integer.parseInt(otherSplit[0]);
        String otherNodeName = otherSplit[1];

        if (otherRoundNumber > this.roundNumber) {
            return false;
        } else if (otherRoundNumber < this.roundNumber) {
            return true;
        } else {
            return otherNodeName.compareTo(this.nodeName.substring(1)) > 0;
        }
    }
}
