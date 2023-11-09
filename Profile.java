public class Profile {
    
    public enum CurrentProfile {
        INSTANT, M2, M3, SMALL, LARGE
    }

    CurrentProfile profile; 


    Profile(String profile) {
        switch (profile.toUpperCase()) {
            case "INSTANT":
                this.profile = CurrentProfile.INSTANT;
                break;
            case "M2":
                this.profile = CurrentProfile.M2;
                break;
            case "M3":
                this.profile = CurrentProfile.M3;
                break;
            case "SMALL":
                this.profile = CurrentProfile.SMALL;
                break;
            case "LARGE":
                this.profile = CurrentProfile.LARGE;
                break;
            default:
                this.profile = CurrentProfile.INSTANT;
                break;
        }
    }

    public int getMessageDelayMilliseconds() {
        switch (this.profile) {
            case INSTANT:
                return 0;
            case M2:
                return 4000;
            case M3:
                return 1000;
            case SMALL:
                return 500;
            case LARGE:
                return 1000;
            default:
                return 0;
        }
    }

    public double getMessageDropProbability() {
        switch (this.profile) {
            case INSTANT:
                return 0.0;
            case M2:
                return 0.2;
            case M3:
                return 0.15;
            case SMALL:
                return 0.05;
            case LARGE:
                return 0.1;
            default:
                return 0.0;
        }
    }


}