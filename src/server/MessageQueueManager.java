package server;

import shared.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Server-side message queue manager.
 * Stores messages for offline users as JSON files in the queued_messages/ directory.
 * When a user comes online, their queued messages are delivered and the file is cleared.
 *
 * JSON format per file (e.g. queued_messages/102.json):
 * [
 *   { "senderId": 101, "receiverId": 102, "message": "Hello!", "timestamp": "2026-10-02T14:30:15" },
 *   ...
 * ]
 *
 * All methods are synchronized to prevent race conditions from multiple ServerClientHandler threads.
 */
public class MessageQueueManager {

    private static final String QUEUE_DIR = "queued_messages";
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Initializes the queued_messages/ directory.
     * Call this once on server startup.
     */
    public static void init() {
        File dir = new File(QUEUE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("[Q] Created queued_messages/ directory");
        }
    }

    /**
     * Queues a message for an offline user.
     * Appends to the receiver's JSON file (e.g. queued_messages/102.json).
     */
    public static synchronized void queueMessage(Message msg) {
        int receiverId = msg.getReceiverId();
        File file = getQueueFile(receiverId);

        // Read existing entries
        String existingJson = readFileContent(file);

        // Build the new JSON entry
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        String entry = buildJsonEntry(msg.getSenderId(), msg.getReceiverId(),
                escapeJson(msg.getString()), timestamp);

        // Append to existing array or create new one
        String updatedJson;
        if (existingJson != null && existingJson.trim().startsWith("[")
                && existingJson.trim().length() > 2) {
            // Remove trailing ] and append new entry
            String trimmed = existingJson.trim();
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
            // Remove trailing comma if present, then add entry
            if (trimmed.endsWith(",")) {
                updatedJson = trimmed + "\n  " + entry + "\n]";
            } else {
                updatedJson = trimmed + ",\n  " + entry + "\n]";
            }
        } else {
            // New file — create fresh array
            updatedJson = "[\n  " + entry + "\n]";
        }

        writeFileContent(file, updatedJson);
        System.out.println("    [Q] Queued message from " + msg.getSenderId()
                + " for offline user " + receiverId);
    }

    /**
     * Returns all queued messages for the given user.
     * Parses the JSON file and reconstructs Message objects.
     */
    public static synchronized List<Message> getQueuedMessages(int userId) {
        List<Message> messages = new ArrayList<>();
        File file = getQueueFile(userId);

        if (!file.exists()) return messages;

        String json = readFileContent(file);
        if (json == null || json.trim().isEmpty()) return messages;

        // Parse JSON array manually
        // Each entry looks like: { "senderId": X, "receiverId": Y, "message": "...", "timestamp": "..." }
        try {
            String trimmed = json.trim();
            if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return messages;

            // Remove outer brackets
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            if (trimmed.isEmpty()) return messages;

            // Split by "}" to get individual entries
            String[] entries = trimmed.split("\\}");

            for (String entryRaw : entries) {
                String entry = entryRaw.trim();
                if (entry.startsWith(",")) entry = entry.substring(1).trim();
                if (!entry.startsWith("{")) continue;
                entry = entry.substring(1).trim(); // remove {

                // Extract fields
                int senderId = extractIntField(entry, "senderId");
                int receiverId = extractIntField(entry, "receiverId");
                String message = extractStringField(entry, "message");

                if (senderId != -1 && receiverId != -1 && message != null) {
                    messages.add(new Message(message, senderId, receiverId));
                }
            }
        } catch (Exception e) {
            System.out.println("[Q] Error parsing queue file for user " + userId + ": " + e.getMessage());
        }

        return messages;
    }

    /**
     * Clears all queued messages for the given user.
     * Deletes the JSON file.
     */
    public static synchronized void clearQueue(int userId) {
        File file = getQueueFile(userId);
        if (file.exists()) {
            file.delete();
            System.out.println("    [Q] Cleared message queue for user " + userId);
        }
    }

    /**
     * Checks if a user has any queued messages.
     */
    public static synchronized boolean hasQueuedMessages(int userId) {
        File file = getQueueFile(userId);
        return file.exists() && file.length() > 2; // more than just "[]"
    }

    // ══════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════

    private static File getQueueFile(int userId) {
        return new File(QUEUE_DIR + File.separator + userId + ".json");
    }

    private static String buildJsonEntry(int senderId, int receiverId,
                                          String message, String timestamp) {
        return "{ \"senderId\": " + senderId
                + ", \"receiverId\": " + receiverId
                + ", \"message\": \"" + message + "\""
                + ", \"timestamp\": \"" + timestamp + "\" }";
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private static String unescapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\\"", "\"")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t")
                   .replace("\\\\", "\\");
    }

    private static int extractIntField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return -1;

        int start = idx + pattern.length();
        // Skip whitespace
        while (start < json.length() && json.charAt(start) == ' ') start++;

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;

        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String extractStringField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;

        int start = idx + pattern.length();
        // Skip whitespace and opening quote
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length() || json.charAt(start) != '"') return null;
        start++; // skip opening "

        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                sb.append(c);
                escaped = false;
            } else if (c == '\\') {
                sb.append(c);
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }

        return unescapeJson(sb.toString());
    }

    private static String readFileContent(File file) {
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            System.out.println("[Q] Error reading file " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private static void writeFileContent(File file, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println("[Q] Error writing file " + file.getName() + ": " + e.getMessage());
        }
    }
}
