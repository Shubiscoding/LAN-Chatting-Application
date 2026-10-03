package server;

import shared.*;
import database.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServerClientHandler implements Runnable {
    private final int userId;
    private final String username;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final ConcurrentHashMap<Integer, ServerClientHandler> connected_clients;
    private final DatabaseConnectivity db;
    private volatile boolean running = true;

    public ServerClientHandler(int userId, String username, Socket socket, ObjectInputStream in, ObjectOutputStream out, ConcurrentHashMap<Integer, ServerClientHandler> connected_clients, DatabaseConnectivity db) {
        this.userId = userId;
        this.username = username;
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.connected_clients = connected_clients;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Message msg = (Message) in.readObject();
                if (msg.getSenderId() == -1 && msg.getReceiverId() == -1) {
                    handleProtocolMessage(msg);
                } else {
                    routeChatMessage(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Error reading message from " + username + ": " + e.getMessage());
        } finally {
            running = false;
            connected_clients.remove(userId);
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private void handleProtocolMessage(Message msg) {
        String content = msg.getString();

        if (content.startsWith("FIND_USER:")) {
            // Client looks for a contact
            try {
                int targetId = Integer.parseInt(content.substring(10));
                
                if (db.UserExists(targetId)) {
                    String targetUsername = db.GetUsername(targetId);
                    sendToClient(new Message("USER_FOUND:" + targetId + ":" + targetUsername));
                    System.out.println(username + " wants to know about ID " + targetId + "|| found: " + targetUsername);
                } else {
                    sendToClient(new Message("USER_NOT_FOUND:" + targetId));
                    System.out.println(username + " wants to know about ID " + targetId + "|| not found");
                }
            } catch (NumberFormatException e) {
                sendToClient(new Message("USER_NOT_FOUND:-1"));
            }
        }
    }

    private void routeChatMessage(Message msg) {
        // send message
        int receiverId = msg.getReceiverId();
        ServerClientHandler target = connected_clients.get(receiverId);

        if (target != null) {
            // if online
            target.sendToClient(msg);
            System.out.println(username + " sent " + target.username + ": " + msg.getString());
        } else {
            // if offline
            MessageQueueManager.queueMessage(msg);
            sendToClient(new Message("QUEUED:" + receiverId));
            System.out.println("Queued message: " + username + " to " + receiverId);
        }
    }

    public synchronized void sendToClient(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message to " + username);
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
