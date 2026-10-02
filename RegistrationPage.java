import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Registration page — same visual style as LoginPage (dark-green branding + form).
 * Connects to the server to register a new user, then navigates to LoginPage.
 */
public class RegistrationPage extends JFrame implements ActionListener {

    // ---------- Color theme (dark green) ----------
    private final Color DARK_GREEN = new Color(0x14, 0x53, 0x2D);
    private final Color MAIN_GREEN = new Color(0x16, 0x65, 0x34);
    private final Color LIGHT_GREEN = new Color(0xDC, 0xFC, 0xE7);
    private final Color WHITE = Color.WHITE;

    // ---------- Connection ----------
    private final String host;
    private final int port;

    // ---------- Components ----------
    private JTextField userIdField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JButton clearButton;

    // Constructor: sets up the registration window
    public RegistrationPage(String host, int port) {
        this.host = host;
        this.port = port;

        setTitle("LAN Chat Application - Register");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createBrandingPanel(), BorderLayout.WEST);
        mainPanel.add(createRegisterPanel(), BorderLayout.CENTER);
        add(mainPanel);
    }

    // ==========================================================
    // LEFT SIDE - BRANDING PANEL
    // ==========================================================
    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(DARK_GREEN);
        panel.setPreferredSize(new Dimension(400, 650));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(60, 40, 60, 40));

        // Chat icon
        JLabel iconLabel = new JLabel("\uD83D\uDCAC");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 70));
        iconLabel.setForeground(WHITE);
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // App name
        JLabel titleLabel = new JLabel("LAN Chat");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Join the Conversation");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(LIGHT_GREEN);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Tagline
        JLabel taglineLabel = new JLabel("Create your account today.");
        taglineLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
        taglineLabel.setForeground(LIGHT_GREEN);
        taglineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(iconLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(subtitleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 40)));
        panel.add(taglineLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // ==========================================================
    // RIGHT SIDE - REGISTRATION FORM
    // ==========================================================
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        int row = 0;

        // ----- Heading -----
        JLabel heading = new JLabel("Create Account");
        heading.setFont(new Font("SansSerif", Font.BOLD, 30));
        heading.setForeground(DARK_GREEN);
        gbc.gridy = row++;
        panel.add(heading, gbc);

        // ----- Subheading -----
        JLabel subheading = new JLabel("Fill in your details to get started");
        subheading.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subheading.setForeground(Color.GRAY);
        gbc.gridy = row++;
        panel.add(subheading, gbc);

        // ----- User ID -----
        JLabel userIdLabel = new JLabel("User ID (number)");
        userIdLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridy = row++;
        gbc.insets = new Insets(18, 10, 4, 10);
        panel.add(userIdLabel, gbc);

        userIdField = new JTextField(20);
        userIdField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        userIdField.setPreferredSize(new Dimension(280, 35));
        userIdField.setBorder(new LineBorder(MAIN_GREEN, 1));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 10, 6, 10);
        panel.add(userIdField, gbc);

        // ----- Username -----
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 10, 4, 10);
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(280, 35));
        usernameField.setBorder(new LineBorder(MAIN_GREEN, 1));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 10, 6, 10);
        panel.add(usernameField, gbc);

        // ----- Password -----
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 10, 4, 10);
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(280, 35));
        passwordField.setBorder(new LineBorder(MAIN_GREEN, 1));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 10, 18, 10);
        panel.add(passwordField, gbc);

        // ----- Buttons -----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonPanel.setBackground(WHITE);

        registerButton = new JButton("REGISTER");
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        registerButton.setBackground(MAIN_GREEN);
        registerButton.setForeground(WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setPreferredSize(new Dimension(130, 40));
        registerButton.setBorder(new EmptyBorder(8, 20, 8, 20));
        registerButton.addActionListener(this);

        clearButton = new JButton("CLEAR");
        clearButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        clearButton.setBackground(WHITE);
        clearButton.setForeground(MAIN_GREEN);
        clearButton.setFocusPainted(false);
        clearButton.setPreferredSize(new Dimension(130, 40));
        clearButton.setBorder(new LineBorder(MAIN_GREEN, 2));
        clearButton.addActionListener(this);

        buttonPanel.add(registerButton);
        buttonPanel.add(clearButton);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 10, 12, 10);
        panel.add(buttonPanel, gbc);

        // ----- Login link -----
        JLabel loginLabel = new JLabel(
                "<html>Already have an account? <font color='#166534'><b>Login here</b></font></html>");
        loginLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loginLabel.setForeground(Color.GRAY);
        loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                goToLogin();
            }
        });
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 10, 10, 10);
        panel.add(loginLabel, gbc);

        return panel;
    }

    // ==========================================================
    // ACTION HANDLING
    // ==========================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerButton) {
            handleRegister();
        } else if (e.getSource() == clearButton) {
            handleClear();
        }
    }

    // Called when the user clicks REGISTER
    private void handleRegister() {
        String userIdStr = userIdField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Validate inputs
        if (userIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a User ID.",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "User ID must be a number.",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username.",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a password.",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 4 characters.",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable button while connecting
        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        // Run network I/O on a background thread to keep UI responsive
        new Thread(() -> {
            try {
                Socket socket = new Socket(host, port);
                Sender sender = new Sender(socket);
                Receiver receiver = new Receiver(socket);

                // Send registration request
                Message regRequest = new Message(
                        "REGISTER:" + userId + ":" + username + ":" + password);
                sender.sendMessage(regRequest);

                // Wait for server response
                Message response = receiver.receiveMessage();
                String result = response.getString();

                socket.close();

                if (result.startsWith("REG_OK:")) {
                    // Parse response: REG_OK:userId:username
                    String[] parts = result.split(":", 3);
                    int id = Integer.parseInt(parts[1]);
                    String uname = parts[2];
                    Profile profile = new Profile(id, uname, "");

                    // Set up user-specific data directory
                    FileManager.setDataDir("data/user_" + id + "/");
                    FileManager.WriteProfile(profile);
                    FileManager.WriteContacts(new ArrayList<>());

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "Registration successful! Welcome, " + uname
                                        + ".\nYou can now login with your User ID and password.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        goToLogin();
                    });

                } else {
                    String errorMsg = result.startsWith("REG_FAIL:")
                            ? result.substring(9)
                            : "Registration failed. Please try again.";

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, errorMsg,
                                "Registration Failed", JOptionPane.ERROR_MESSAGE);
                        registerButton.setEnabled(true);
                        registerButton.setText("REGISTER");
                    });
                }

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Could not connect to server at " + host + ":" + port
                                    + "\n" + e.getMessage(),
                            "Connection Error", JOptionPane.ERROR_MESSAGE);
                    registerButton.setEnabled(true);
                    registerButton.setText("REGISTER");
                });
            }
        }).start();
    }

    // Called when the Clear button is pressed
    private void handleClear() {
        userIdField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        userIdField.requestFocus();
    }

    // Navigate back to LoginPage
    private void goToLogin() {
        dispose();
        LoginPage loginPage = new LoginPage(host, port);
        loginPage.setVisible(true);
    }
}
