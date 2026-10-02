import javax.swing.SwingUtilities;

/**
 * Client-side message handler.
 * Runs on a background thread, continuously listening for incoming messages.
 * Routes chat messages to the UI and handles protocol responses from the server.
 */
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

            if (message == null) {
                // Connection lost
                running = false;
                break;
            }

            // Protocol messages have sender_id == -1 and receiver_id == -1
            if (message.getSenderId() == -1 && message.getReceiverId() == -1) {
                handleProtocolMessage(message);
            } else {
                handleChatMessage(message);
            }
        }
    }

    /**
     * Handles protocol responses from the server.
     * These include: USER_FOUND, USER_NOT_FOUND, OFFLINE notifications.
     */
    private void handleProtocolMessage(Message message) {
        String content = message.getString();

        SwingUtilities.invokeLater(() -> {
            if (content.startsWith("USER_FOUND:")) {
                // Server found the user we searched for
                String[] parts = content.split(":", 3);
                int id = Integer.parseInt(parts[1]);
                String username = parts[2];
                winApp.onContactFound(new Profile(id, username, ""));

            } else if (content.startsWith("USER_NOT_FOUND:")) {
                winApp.onContactNotFound();

            } else if (content.startsWith("OFFLINE:")) {
                int offlineId = Integer.parseInt(content.substring(8));
                winApp.onUserOffline(offlineId);
            }
        });
    }

    /**
     * Handles incoming chat messages — forwards to the UI.
     */
    private void handleChatMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            winApp.onMessageReceived(message);
        });
    }

    /**
     * Sends a chat message to the server for routing.
     */
    void sendMessage(Message message) {
        sender.sendMessage(message);
    }

    /**
     * Sends a protocol message to the server (e.g., FIND_USER:123).
     * Protocol messages use the Message(String) constructor which sets ids to -1.
     */
    void sendProtocolMessage(String protocolContent) {
        sender.sendMessage(new Message(protocolContent));
    }

    void stop() {
        running = false;
    }
}
