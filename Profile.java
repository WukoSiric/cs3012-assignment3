public class Profile {
    
    public enum CurrentProfile {
        INSTANT, M2, M3, SMALL, LARGE
    }

    CurrentProfile profile; 
    Profile(String profile) {
        switch (profile.toLowerCase()) {
            case "instant":
                this.profile = CurrentProfile.INSTANT;
                break;
            case "m2":
                this.profile = CurrentProfile.M2;
                break;
            case "m3":
                this.profile = CurrentProfile.M3;
                break;
            case "small":
                this.profile = CurrentProfile.SMALL;
                break;
            case "large":
                this.profile = CurrentProfile.LARGE;
                break;
            default:
                this.profile = CurrentProfile.INSTANT;
                break;
        }
    }

}