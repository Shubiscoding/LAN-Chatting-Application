package client;

import shared.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Sender {
    private final ObjectOutputStream out;

    public Sender(Socket socket) throws IOException {
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
    }

    public synchronized void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
