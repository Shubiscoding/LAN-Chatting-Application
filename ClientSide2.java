import javax.swing.*;

/**
 * Second client entry point — connects to the server via LAN IP.
 * Use this to test two-person chat on a single laptop.
 *
 * Both ClientSide (localhost) and ClientSide2 (LAN IP) connect
 * to the same server running on this machine.
 *
 * Run ServerSide first, then run ClientSide and ClientSide2 separately.
 */
public class ClientSide2 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage("192.168.29.221", 5000);
            loginPage.setVisible(true);
        });
    }
}
