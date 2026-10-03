package server;

import shared.*;
import database.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pure relay server — no UI, no chat window.
 * Accepts multiple client connections, validates login/registration,
 * and routes messages between connected clients.
 *
 * Run this first, then start ClientSide and/or ClientSide2.
 */
public class ServerSide {

    private static final int PORT = 5000;
    private static final ConcurrentHashMap<Integer, ServerClientHandler> connectedClients = new ConcurrentHashMap<>();
    private static DatabaseConnectivity db;

    public static void main(String[] args) {
        // Initialize database
        db = new DatabaseConnectivity();
        db.StartSql();

        // Initialize the queued messages directory
        MessageQueueManager.init();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("══════════════════════════════════════════");
            System.out.println("   LAN Chat Server started on port " + PORT);
            System.out.println("   Waiting for clients to connect...");
            System.out.println("══════════════════════════════════════════");

            // Accept connections in an infinite loop
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[+] New connection from: " + clientSocket.getInetAddress().getHostAddress());

                // Handle each connection on a new thread
                new Thread(() -> handleNewConnection(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.CloseSql();
        }
    }

    /**
     * Handles a new client connection.
     * Reads the first message to determine if it's a LOGIN or REGISTER request.
     */
    private static void handleNewConnection(Socket socket) {
        try {
            // Create streams (ObjectOutputStream must be created and flushed first
            // to send the stream header before ObjectInputStream reads it on the other side)
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Read the first message — it's always an auth/registration request
            Message firstMsg = (Message) in.readObject();
            String content = firstMsg.getString();

            if (content.startsWith("LOGIN:")) {
                handleLogin(socket, in, out, content);
            } else if (content.startsWith("REGISTER:")) {
                handleRegistration(out, content);
                socket.close();
            } else {
                out.writeObject(new Message("ERROR:Unknown request"));
                out.flush();
                socket.close();
            }

        } catch (Exception e) {
            System.out.println("[-] Connection handling error: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Handles a LOGIN request.
     * Format: "LOGIN:userId:password"
     * On success, creates a ServerClientHandler and blocks until the client disconnects.
     */
    private static void handleLogin(Socket socket, ObjectInputStream in,
                                    ObjectOutputStream out, String content) throws IOException {
        String[] parts = content.split(":", 3);

        if (parts.length != 3) {
            out.writeObject(new Message("AUTH_FAIL:Invalid login format"));
            out.flush();
            socket.close();
            return;
        }

        try {
            int userId = Integer.parseInt(parts[1]);
            String password = parts[2];

            // Check if already logged in
            if (connectedClients.containsKey(userId)) {
                out.writeObject(new Message("AUTH_FAIL:This account is already logged in elsewhere"));
                out.flush();
                socket.close();
                return;
            }

            // Validate credentials
            boolean valid = db.isLogin(userId, password);

            if (valid) {
                String username = db.GetUsername(userId);
                out.writeObject(new Message("AUTH_OK:" + userId + ":" + username));
                out.flush();

                System.out.println("[✓] User '" + username + "' (ID: " + userId + ") logged in");

                // Deliver any queued messages from when this user was offline
                List<Message> pending = MessageQueueManager.getQueuedMessages(userId);
                if (!pending.isEmpty()) {
                    for (Message m : pending) {
                        out.writeObject(m);
                        out.flush();
                    }
                    MessageQueueManager.clearQueue(userId);
                    System.out.println("[📬] Delivered " + pending.size() + " queued message(s) to " + username);
                }

                // Create a per-client handler and register it
                ServerClientHandler handler = new ServerClientHandler(
                        userId, username, socket, in, out, connectedClients, db
                );
                connectedClients.put(userId, handler);

                // This call blocks until the client disconnects
                handler.run();

                // Client disconnected
                connectedClients.remove(userId);
                System.out.println("[✗] User '" + username + "' (ID: " + userId + ") disconnected");

            } else {
                out.writeObject(new Message("AUTH_FAIL:Invalid User ID or password"));
                out.flush();
                socket.close();
            }

        } catch (NumberFormatException e) {
            out.writeObject(new Message("AUTH_FAIL:User ID must be a number"));
            out.flush();
            socket.close();
        }
    }

    /**
     * Handles a REGISTER request.
     * Format: "REGISTER:userId:username:password"
     * After sending the response, the socket is closed (registration is stateless).
     */
    private static void handleRegistration(ObjectOutputStream out, String content) throws IOException {
        String[] parts = content.split(":", 4);

        if (parts.length != 4) {
            out.writeObject(new Message("REG_FAIL:Invalid registration format"));
            out.flush();
            return;
        }

        try {
            int userId = Integer.parseInt(parts[1]);
            String username = parts[2];
            String password = parts[3];

            if (db.UserExists(userId)) {
                out.writeObject(new Message("REG_FAIL:User ID " + userId + " already exists"));
            } else {
                boolean success = db.RegisterUser(userId, username, password);
                if (success) {
                    out.writeObject(new Message("REG_OK:" + userId + ":" + username));
                    System.out.println("[+] Registered new user: '" + username + "' (ID: " + userId + ")");
                } else {
                    out.writeObject(new Message("REG_FAIL:Registration failed. Please try again."));
                }
            }
            out.flush();

        } catch (NumberFormatException e) {
            out.writeObject(new Message("REG_FAIL:User ID must be a number"));
            out.flush();
        }
    }
}
