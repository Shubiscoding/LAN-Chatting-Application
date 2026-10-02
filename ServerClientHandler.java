import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles a single connected client on the server side.
 * Reads messages from the client, routes chat messages to recipients,
 * and handles protocol messages (e.g., FIND_USER).
 */
public class ServerClientHandler implements Runnable {

    private final int userId;
    private final String username;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final ConcurrentHashMap<Integer, ServerClientHandler> connectedClients;
    private final DatabaseConnectivity db;
    private volatile boolean running = true;

    public ServerClientHandler(int userId, String username, Socket socket,
                               ObjectInputStream in, ObjectOutputStream out,
                               ConcurrentHashMap<Integer, ServerClientHandler> connectedClients,
                               DatabaseConnectivity db) {
        this.userId = userId;
        this.username = username;
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.connectedClients = connectedClients;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Message msg = (Message) in.readObject();

                // Protocol messages have sender_id == -1 and receiver_id == -1
                if (msg.getSenderId() == -1 && msg.getReceiverId() == -1) {
                    handleProtocolMessage(msg);
                } else {
                    routeChatMessage(msg);
                }
            }
        } catch (IOException e) {
            // Client disconnected (socket closed or broken pipe)
        } catch (ClassNotFoundException e) {
            System.out.println("[-] Error reading message from " + username + ": " + e.getMessage());
        } finally {
            running = false;
            connectedClients.remove(userId);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Handles non-chat protocol messages (system commands).
     * Protocol messages use the Message(String) constructor which sets ids to -1.
     */
    private void handleProtocolMessage(Message msg) {
        String content = msg.getString();

        if (content.startsWith("FIND_USER:")) {
            // Client is looking up a user to add as contact
            try {
                int targetId = Integer.parseInt(content.substring(10));

                if (db.UserExists(targetId)) {
                    String targetUsername = db.GetUsername(targetId);
                    sendToClient(new Message("USER_FOUND:" + targetId + ":" + targetUsername));
                    System.out.println("    [?] " + username + " looked up user ID " + targetId + " → found '" + targetUsername + "'");
                } else {
                    sendToClient(new Message("USER_NOT_FOUND:" + targetId));
                    System.out.println("    [?] " + username + " looked up user ID " + targetId + " → not found");
                }
            } catch (NumberFormatException e) {
                sendToClient(new Message("USER_NOT_FOUND:-1"));
            }
        }
    }

    /**
     * Routes a chat message to the intended recipient.
     * If the recipient is online, delivers immediately.
     * If offline, notifies the sender.
     */
    private void routeChatMessage(Message msg) {
        int receiverId = msg.getReceiverId();
        ServerClientHandler target = connectedClients.get(receiverId);

        if (target != null) {
            target.sendToClient(msg);
            System.out.println("    [→] " + username + " → " + target.username + ": " + msg.getString());
        } else {
            // Receiver is offline — notify sender
            sendToClient(new Message("OFFLINE:" + receiverId));
            System.out.println("    [!] Message from " + username + " to offline user ID " + receiverId);
        }
    }

    /**
     * Sends a message to this handler's connected client.
     * Synchronized to prevent concurrent writes to the output stream.
     */
    public synchronized void sendToClient(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("[-] Failed to send message to " + username);
            running = false;
        }
    }

    public void stop() {
        running = false;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
