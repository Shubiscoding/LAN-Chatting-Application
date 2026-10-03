package filemanagement;

import shared.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handles all file I/O for the application.
 * Uses ObjectInputStream and ObjectOutputStream for serialized Java objects.
 *
 * Added: setDataDir() to isolate each user's data into separate directories,
 *        allowing multiple clients to run on the same machine without conflicts.
 * Added: WriteProfile() to save the user's profile to disk.
 */
public class FileManager {

    // Data directory prefix — set per user to isolate file storage
    private static String dataDir = "";

    // Subdirectory for human-readable chat logs
    private static final String SAVED_CHATS_DIR = "saved_chats";

    // Timestamp format for chat log entries
    private static final DateTimeFormatter CHAT_TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Sets the directory where all .dat files are stored.
     * Creates the directory if it doesn't exist.
     * Call this after login/registration, before loading any data.
     */
    public static void setDataDir(String dir) {
        dataDir = dir;
        new File(dir).mkdirs();
        // Also create the saved_chats/ subdirectory
        new File(dir + SAVED_CHATS_DIR).mkdirs();
    }

    /**
     * Returns the current data directory prefix.
     */
    public static String getDataDir() {
        return dataDir;
    }

    public static ArrayList<Message> LoadWaitingMessages() {

        ArrayList<Message> messages = new ArrayList<>();

        try {
            ObjectInputStream input =
                    new ObjectInputStream(
                            new FileInputStream(dataDir + "waiting.dat"));

            messages = (ArrayList<Message>) input.readObject();

            input.close();

        } catch (FileNotFoundException e) {
            System.out.println("waiting.dat not found.");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return messages;
    }


    public static ArrayList<Profile> LoadContacts() {

        ArrayList<Profile> contacts = new ArrayList<>();

        try {
            ObjectInputStream input =
                    new ObjectInputStream(
                            new FileInputStream(dataDir + "contactList.dat"));

            contacts = (ArrayList<Profile>) input.readObject();

            input.close();

        } catch (FileNotFoundException e) {
            System.out.println("contactList.dat not found.");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return contacts;
    }


    public static HashMap<Integer, ArrayList<Message>> LoadMessages() {

        HashMap<Integer, ArrayList<Message>> chats =
                new HashMap<>();

        try {
            ObjectInputStream input =
                    new ObjectInputStream(
                            new FileInputStream(dataDir + "saveChats.dat"));

            chats =
                    (HashMap<Integer, ArrayList<Message>>) input.readObject();

            input.close();

        } catch (FileNotFoundException e) {
            System.out.println("saveChats.dat not found.");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return chats;
    }


    public static Profile LoadProfile() {

        Profile profile = null;

        try {
            ObjectInputStream input =
                    new ObjectInputStream(
                            new FileInputStream(dataDir + "profile.dat"));

            profile = (Profile) input.readObject();

            input.close();

        } catch (FileNotFoundException e) {
            System.out.println("profile.dat not found.");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return profile;
    }


    public static synchronized void WriteContacts(
            ArrayList<Profile> contacts) {

        try {
            ObjectOutputStream output =
                    new ObjectOutputStream(
                            new FileOutputStream(dataDir + "contactList.dat"));

            output.writeObject(contacts);

            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static synchronized void WriteMessage(
            int contactId, Message message) {

        HashMap<Integer, ArrayList<Message>> chats =
                LoadMessages();

        if (!chats.containsKey(contactId)) {
            chats.put(contactId, new ArrayList<Message>());
        }

        chats.get(contactId).add(message);

        try {
            ObjectOutputStream output =
                    new ObjectOutputStream(
                            new FileOutputStream(dataDir + "saveChats.dat"));

            output.writeObject(chats);

            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the current user's profile to profile.dat.
     */
    public static synchronized void WriteProfile(Profile profile) {

        try {
            ObjectOutputStream output =
                    new ObjectOutputStream(
                            new FileOutputStream(dataDir + "profile.dat"));

            output.writeObject(profile);

            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a message to a human-readable chat log file.
     * Each contact has their own file: saved_chats/<contactId>_chats.txt
     * Messages are appended with timestamps in the format:
     *   [2026-10-02 14:30:15] You: Hello!
     *   [2026-10-02 14:30:22] Shubh: Hi there!
     *
     * @param contactId       the ID of the contact this conversation is with
     * @param message         the Message object being saved
     * @param contactUsername the display name of the contact
     * @param isSent          true if this message was sent by the current user
     */
    public static synchronized void SaveChatToFile(
            int contactId, Message message, String contactUsername, boolean isSent) {

        String filePath = dataDir + SAVED_CHATS_DIR + File.separator
                + contactId + "_chats.txt";

        String timestamp = LocalDateTime.now().format(CHAT_TIMESTAMP_FMT);
        String sender = isSent ? "You" : contactUsername;
        String line = "[" + timestamp + "] " + sender + ": " + message.getString();

        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(line + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error saving chat to file: " + e.getMessage());
        }
    }

    /**
     * Loads the human-readable chat log for a specific contact.
     * Returns all lines from saved_chats/<contactId>_chats.txt.
     *
     * @param contactId the ID of the contact
     * @return list of chat log lines, or empty list if file doesn't exist
     */
    public static ArrayList<String> LoadChatFromFile(int contactId) {

        ArrayList<String> lines = new ArrayList<>();
        String filePath = dataDir + SAVED_CHATS_DIR + File.separator
                + contactId + "_chats.txt";

        File file = new File(filePath);
        if (!file.exists()) return lines;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error loading chat from file: " + e.getMessage());
        }

        return lines;
    }
}