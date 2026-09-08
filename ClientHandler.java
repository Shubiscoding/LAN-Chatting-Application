import javax.swing.SwingUtilities;

public class ClientHandler implements Runnable {
    private final Sender sender;
    private final WindowApplication winApp;
    private final Receiver receiver;
    private volatile boolean running = true;

    ClientHandler(WindowApplication winApp, Sender sender, Receiver receiver) {
        this.winApp = winApp;
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        while (running) {
            Message message = receiver.receiveMessage();
            if (message != null) {
                displayMessage(message);
            }
        }
    }

    void displayMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            winApp.displayReceivedMessage(message);
        });
    }

    void sendMessage(Message message) {
        sender.sendMessage(message);
    }

    void stop() {
        running = false;
    }
}
