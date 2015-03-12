package common;

import java.io.Serializable;

/**
 * Created by Ludde on 2015-03-12.
 */
public final class Message implements Serializable{

    private final String userNameFrom;
    private final String userNameTo;
    private final String message;

    public Message(String userNameFrom, String userNameTo, String message) {
        this.userNameFrom = userNameFrom;
        this.userNameTo = userNameTo;
        this.message = message;
    }

    public String getUserNameFrom() {
        return userNameFrom;
    }

    public String getUserNameTo() {
        return userNameTo;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        return "From: " + userNameFrom + " To: " + userNameTo + " Message: " + message;
    }
}
