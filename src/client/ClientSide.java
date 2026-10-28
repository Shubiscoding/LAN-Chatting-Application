package client;

import ui.*;
import javax.swing.*;
import shared.*;

public class ClientSide {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage("10.172.204.192", 4999);
            loginPage.setVisible(true);
        });
    }
}
