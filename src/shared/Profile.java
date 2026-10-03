package shared;

import java.io.Serializable;

public class Profile implements Serializable {

    private int id;
    private String username;
    private String profilePicture;

    public Profile(int id, String username, String profilePicture) {
        this.id = id;
        this.username = username;
        this.profilePicture = profilePicture;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
}