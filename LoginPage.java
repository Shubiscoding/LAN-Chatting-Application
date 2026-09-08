import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/*
 * LoginPage.java
 * ---------------
 * This is the LOGIN PAGE for our LAN Chat Application project.
 * It is built using ONLY standard Java Swing and AWT classes.
 *
 * The window is divided into two parts:
 *   1. LEFT  -> Branding Panel (dark green, shows app name/logo)
 *   2. RIGHT -> Login Panel (white, contains the actual login form)
 *
 * NOTE: The real authentication / LAN connection logic is NOT
 * implemented here. That will be added later by the networking part
 * of the project. Right now, this class only handles the GUI and
 * basic input validation.
 */
public class LoginPage extends JFrame implements ActionListener {

    // ---------- Color theme (dark green) ----------
    private final Color DARK_GREEN = new Color(0x14, 0x53, 0x2D);   // #14532D
    private final Color MAIN_GREEN = new Color(0x16, 0x65, 0x34);   // #166534
    private final Color LIGHT_GREEN = new Color(0xDC, 0xFC, 0xE7);  // #DCFCE7
    private final Color WHITE = Color.WHITE;

    // ---------- Components that we need to access in multiple methods ----------
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton clearButton;

    // Constructor: sets up the whole window
    public LoginPage() {

        // ----- Basic JFrame settings -----
        setTitle("LAN Chat Application - Login");
        setSize(1000, 650);
        setLocationRelativeTo(null);              // center the window on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ----- Main panel using BorderLayout -----
        // BorderLayout divides the frame into WEST, CENTER, EAST, NORTH, SOUTH.
        // We only need WEST (branding) and CENTER (login form).
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel brandingPanel = createBrandingPanel();
        JPanel loginPanel = createLoginPanel();

        mainPanel.add(brandingPanel, BorderLayout.WEST);
        mainPanel.add(loginPanel, BorderLayout.CENTER);

        // Add the main panel to the frame
        add(mainPanel);
    }

    // ==========================================================
    // LEFT SIDE - BRANDING PANEL
    // ==========================================================
    private JPanel createBrandingPanel() {

        JPanel panel = new JPanel();
        panel.setBackground(DARK_GREEN);
        panel.setPreferredSize(new Dimension(400, 650)); // roughly 40% of 1000 width

        // BoxLayout stacks components vertically, one below the other.
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(60, 40, 60, 40));

        // ----- Chat icon (using a Unicode emoji, no external image needed) -----
        JLabel iconLabel = new JLabel("\uD83D\uDCAC"); // 💬 speech bubble emoji
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 70));
        iconLabel.setForeground(WHITE);
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ----- App name -----
        JLabel titleLabel = new JLabel("LAN Chat");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ----- Subtitle -----
        JLabel subtitleLabel = new JLabel("Communication Made Simple");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(LIGHT_GREEN);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ----- Tagline -----
        JLabel taglineLabel = new JLabel("Connect. Chat. Collaborate.");
        taglineLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
        taglineLabel.setForeground(LIGHT_GREEN);
        taglineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add some spacing between the labels
        panel.add(iconLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(subtitleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 40)));
        panel.add(taglineLabel);

        // Push everything up (optional glue at bottom)
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // ==========================================================
    // RIGHT SIDE - LOGIN PANEL
    // ==========================================================
    private JPanel createLoginPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);

        // GridBagConstraints let us control exactly where each
        // component goes inside the GridBagLayout grid.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); // spacing around each component
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;

        int row = 0;

        // ----- Heading -----
        JLabel heading = new JLabel("Welcome Back");
        heading.setFont(new Font("SansSerif", Font.BOLD, 30));
        heading.setForeground(DARK_GREEN);
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(heading, gbc);

        // ----- Subheading -----
        JLabel subheading = new JLabel("Login to continue chatting");
        subheading.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subheading.setForeground(Color.GRAY);
        gbc.gridy = row++;
        panel.add(subheading, gbc);

        // ----- Username label -----
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridy = row++;
        gbc.insets = new Insets(20, 10, 4, 10);
        panel.add(usernameLabel, gbc);

        // ----- Username field -----
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(280, 35));
        usernameField.setBorder(new LineBorder(MAIN_GREEN, 1));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 10, 8, 10);
        panel.add(usernameField, gbc);

        // ----- Password label -----
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridy = row++;
        gbc.insets = new Insets(10, 10, 4, 10);
        panel.add(passwordLabel, gbc);

        // ----- Password field -----
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(280, 35));
        passwordField.setBorder(new LineBorder(MAIN_GREEN, 1));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 10, 20, 10);
        panel.add(passwordField, gbc);

        // Pressing ENTER inside the password field triggers login
        passwordField.addActionListener(this);

        // ----- Buttons panel (Login + Clear side by side) -----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonPanel.setBackground(WHITE);

        loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setBackground(MAIN_GREEN);
        loginButton.setForeground(WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.setBorder(new EmptyBorder(8, 20, 8, 20));
        loginButton.addActionListener(this);

        clearButton = new JButton("CLEAR");
        clearButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        clearButton.setBackground(WHITE);
        clearButton.setForeground(MAIN_GREEN);
        clearButton.setFocusPainted(false);
        clearButton.setPreferredSize(new Dimension(120, 40));
        clearButton.setBorder(new LineBorder(MAIN_GREEN, 2));
        clearButton.addActionListener(this);

        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 10, 15, 10);
        panel.add(buttonPanel, gbc);

        // ----- Footer text -----
        JLabel footerLabel = new JLabel("Enter your credentials to join the LAN chat.");
        footerLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        footerLabel.setForeground(Color.GRAY);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 10, 10, 10);
        panel.add(footerLabel, gbc);

        return panel;
    }

    // ==========================================================
    // ACTION HANDLING (Login button, Clear button, Enter key)
    // ==========================================================
    @Override
    public void actionPerformed(ActionEvent e) {

        // This method runs whenever the Login button, Clear button,
        // or the Enter key (inside the password field) is triggered.

        if (e.getSource() == loginButton || e.getSource() == passwordField) {
            handleLogin();
        } else if (e.getSource() == clearButton) {
            handleClear();
        }
    }

    // Called when the user tries to log in
    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your username.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your password.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Actual authentication and LAN connection logic will be added here later

        JOptionPane.showMessageDialog(this,
                "Login successful! Welcome, " + username,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Called when the Clear button is pressed
    private void handleClear() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus(); // put cursor back in username field
    }

    // ==========================================================
    // MAIN METHOD - program starts here
    // ==========================================================
    public static void main(String[] args) {
        // SwingUtilities.invokeLater makes sure the GUI is created
        // safely on the Event Dispatch Thread (standard Swing practice).
        SwingUtilities.invokeLater(() -> {
            LoginPage frame = new LoginPage();
            frame.setVisible(true);
        });
    }
}
