import java.io.*;
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

    /**
     * Sets the directory where all .dat files are stored.
     * Creates the directory if it doesn't exist.
     * Call this after login/registration, before loading any data.
     */
    public static void setDataDir(String dir) {
        dataDir = dir;
        new File(dir).mkdirs();
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
}