package server;

import shared.*;
import database.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSide {
    private static final int port = 5000;
    private static final ConcurrentHashMap<Integer, ServerClientHandler> connected_clients = new ConcurrentHashMap<>();
    private static DatabaseConnectivity db;

    public static void main(String[] args) {
        // Init dbms
        db = new DatabaseConnectivity();
        db.StartSql(); 

        // checks message queue
        MessageQueueManager.init(); 

        try {
            // Creates a server Socket
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server Started successfully! PORT:" + port);
            System.out.println("Waiting for clients to connect...");

            // The server accepts client sockets 
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection: " + clientSocket.getInetAddress().getHostAddress());
                new Thread(() -> handleNewConnection(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.CloseSql();
        }
    }

    private static void handleNewConnection(Socket socket) {
        try {
            // Basically as soon as new client joins, the server takes it request for either Login or Register
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Gets the request
            Message firstMsg = (Message) in.readObject();
            String content = firstMsg.getString();

            // Checks the request
            if (content.startsWith("LOGIN:")) {
                handleLogin(socket, in, out, content);
            } else if (content.startsWith("REGISTER:")) {
                handleRegistration(out, content);
                socket.close();
            } else {
                out.flush();
                socket.close();
            }

        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private static void handleLogin(Socket socket, ObjectInputStream in, ObjectOutputStream out, String content) throws IOException {
        // Login format: "LOGIN:username:password"
        String[] parts = content.split(":", 3);

        if (parts.length != 3) {
            // Security check
            out.writeObject(new Message("AUTH_FAIL:Invalid login format"));
            out.flush();
            socket.close();
            return;
        }

        String username = parts[1];
        String password = parts[2];

        // Resolve username -> userId and validate password in one DB call
        int userId = db.isLoginByUsername(username, password);

        if (userId == -1) {
            out.writeObject(new Message("AUTH_FAIL:Invalid username or password"));
            out.flush();
            socket.close();
            return;
        }

        // Check if already logged in
        if (connected_clients.containsKey(userId)) {
            out.writeObject(new Message("AUTH_FAIL:This account is already logged in elsewhere"));
            out.flush();
            socket.close();
            return;
        }

        out.writeObject(new Message("AUTH_OK:" + userId + ":" + username));
        out.flush();

        System.out.println("User:" + username + " with ID:" + userId + " logged in!");

        // Send queued messages (if any)
        List<Message> pending = MessageQueueManager.getQueuedMessages(userId);
        if (!pending.isEmpty()) {
            for (Message m : pending) {
                out.writeObject(m);
                out.flush();
            }
            MessageQueueManager.clearQueue(userId);
        }

        // Creates helper class (this has a lot of helper functions for the server)
        ServerClientHandler handler = new ServerClientHandler(userId, username, socket, in, out, connected_clients, db);
        connected_clients.put(userId, handler);
        handler.run();
        System.out.println("User:" + username + " with ID:" + userId + " disconnected T_T");
    }

    private static void handleRegistration(ObjectOutputStream out, String content) throws IOException {
        String[] parts = content.split(":", 3);
        // Format: "REGISTER:username:password"

        if (parts.length != 3) {
            out.writeObject(new Message("REG_FAIL:Invalid registration format"));
            out.flush();
            return;
        }

        String username = parts[1];
        String password = parts[2];

        if (db.GetUserId(username) != -1) {
            out.writeObject(new Message("REG_FAIL:Username '" + username + "' is already taken"));
        } else {
            int newId = db.RegisterUser(username, password);
            if (newId != -1) {
                out.writeObject(new Message("REG_OK:" + newId + ":" + username));
                System.out.println("Registered new user: " + username + " with auto-assigned ID: " + newId);
            } else {
                out.writeObject(new Message("REG_FAIL:Registration failed. Please try again."));
            }
        }
        out.flush();
    }
}
