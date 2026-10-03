package client;

import ui.*;
import javax.swing.*;

/**
 * Client entry point — connects to the server via localhost.
 * Use this when the server is running on the same machine.
 *
 * Run ServerSide first, then run this.
 */
public class ClientSide {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage("localhost", 5000);
            loginPage.setVisible(true);
        });
    }
}
