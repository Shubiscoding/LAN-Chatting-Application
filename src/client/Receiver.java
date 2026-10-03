package client;

import shared.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class Receiver {
    private final ObjectInputStream in;

    public Receiver(Socket socket) throws IOException {
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public Message receiveMessage() {
        try {
            return (Message) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
