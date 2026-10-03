package client;

import ui.*;
import javax.swing.*;
import shared.*;

public class ClientSide {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage("localhost", 5000);
            loginPage.setVisible(true);
        });
    }
}
