import java.net.Socket;

public class ClientSide {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 5000);
            WindowApplication window = new WindowApplication();
            Sender sender = new Sender(socket);
            Receiver receiver = new Receiver(socket);

            ClientHandler handler = new ClientHandler(window, sender, receiver);
            window.setClientHandler(handler);

            Thread thread = new Thread(handler);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
