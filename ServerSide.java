import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSide {
    public static void main(String[] args) {

        try {
            System.out.println("Waiting for people to join...");
            ServerSocket serverSocket = new ServerSocket(5000);
            Socket socket = serverSocket.accept();

            System.out.println("Client received");
            WindowApplication window = new WindowApplication();
            Sender sender = new Sender(socket);
            Receiver receiver = new Receiver(socket);

            ClientHandler handler = new ClientHandler(window, sender, receiver);
            window.setClientHandler(handler);

            Thread thread = new Thread(handler);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
