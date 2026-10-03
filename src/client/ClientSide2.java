package client;

import ui.*;
import javax.swing.*;

public class ClientSide2 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage("192.168.29.221", 5000);
            loginPage.setVisible(true);
        });
    }
}
