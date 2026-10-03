package client;

import shared.*;
import ui.*;
import javax.swing.SwingUtilities;

public class ClientHandler implements Runnable {
    private final Sender sender;
    private final WindowApplication winApp;
    private final Receiver receiver;
    private volatile boolean running = true;

    public ClientHandler(WindowApplication winApp, Sender sender, Receiver receiver) {
        this.winApp = winApp;
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        while (running) {
            Message message = receiver.receiveMessage();

            if (message == null) {
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

            } else if (content.startsWith("QUEUED:")) {
                int queuedId = Integer.parseInt(content.substring(7));
                winApp.onMessageQueued(queuedId);
            }
        });
    }

    private void handleChatMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            winApp.onMessageReceived(message);
        });
    }

    public void sendMessage(Message message) {
        sender.sendMessage(message);
    }

    public void sendProtocolMessage(String protocolContent) {
        sender.sendMessage(new Message(protocolContent));
    }

    public void stop() {
        running = false;
    }
}
