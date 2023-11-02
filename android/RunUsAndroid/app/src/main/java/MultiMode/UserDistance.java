package MultiMode;

import java.io.Serializable;

public class UserDistance implements Serializable {


    MultiModeUser user;

    float distance;

    public UserDistance(MultiModeUser user, float distance) {
        this.user = user;
        this.distance = distance;
    }

    public MultiModeUser getUser() {
        return user;
    }

    public void setUser(MultiModeUser user) {
        this.user = user;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
