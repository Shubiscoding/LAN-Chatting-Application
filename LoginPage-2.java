import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPage extends JFrame implements ActionListener {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton clearButton;

    public LoginPage() {

        setTitle("LAN Chat - Login");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Left panel
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(28, 42, 68));
        leftPanel.setPreferredSize(new Dimension(250, 400));

        JLabel title = new JLabel("LAN CHAT");
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(Color.WHITE);

        JLabel tagline = new JLabel("Local Network Messaging");
        tagline.setForeground(Color.WHITE);

        leftPanel.add(title);
        leftPanel.add(tagline);


        // Right panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(new Font("Arial", Font.BOLD, 22));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(280, 35));
        usernameField.setPreferredSize(new Dimension(280, 35));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);


        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(280, 35));
        passwordField.setPreferredSize(new Dimension(280, 35));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);


        loginButton = new JButton("LOGIN");
        clearButton = new JButton("CLEAR");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Add components
        rightPanel.add(Box.createVerticalStrut(25));
        rightPanel.add(welcome);

        rightPanel.add(Box.createVerticalStrut(25));
        rightPanel.add(usernameLabel);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(usernameField);

        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(passwordLabel);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(passwordField);

        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(buttonPanel);


        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel);


        // Button actions
        loginButton.addActionListener(this);
        clearButton.addActionListener(this);
        passwordField.addActionListener(this);

        setVisible(true);
    }


    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == loginButton ||
                e.getSource() == passwordField) {

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Username and Password cannot be empty!"
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Login Successful!\nWelcome " + username
                );

                System.out.println("Username: " + username);

                dispose();
            }
        }

        if (e.getSource() == clearButton) {

            usernameField.setText("");
            passwordField.setText("");
            usernameField.requestFocus();
        }
    }


    public static void main(String[] args) {
        new LoginPage();
    }
}