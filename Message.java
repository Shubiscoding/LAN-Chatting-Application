import java.io.Serializable;

public class Message implements Serializable {
    private final String message;
    private final int sender_id;
    private final int receiver_id;

    public Message(String message, int sender_id, int receiver_id) {
        this.message = message;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
    }

    public Message(String message) {
        this.message = message;
        this.sender_id = -1;
        this.receiver_id = -1;
    }

    public String getString() {
        return message;
    }

    public int getSenderId() {
        return sender_id;
    }

    public int getReceiverId() {
        return receiver_id;
    }
}
