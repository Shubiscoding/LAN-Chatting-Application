import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class Receiver {
    private final ObjectInputStream in;

    Receiver(Socket socket) throws IOException {
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    Message receiveMessage() {
        try {
            return (Message) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    void close() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
