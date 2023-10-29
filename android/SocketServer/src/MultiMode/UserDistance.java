package MultiMode;

import java.io.Serializable;

public class UserDistance  implements Serializable {


    MultiModeUser user;

    double distance;

    public UserDistance(MultiModeUser user, double distance){
        this.user = user;
        this.distance = distance;
    }
    public MultiModeUser getUser() {
        return user;
    }

    public void setUser(MultiModeUser user) {
        this.user = user;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
