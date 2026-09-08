import javax.swing.*;
import java.awt.*;

public class WindowApplication {
    private JFrame frame;
    private JTextField textField;
    private JPanel messagePanel;
    private ClientHandler clientHandler;

    WindowApplication() {
        createWindow();
    }

    void createWindow() {
        frame = new JFrame("Chat");
        frame.setSize(450, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(messagePanel);
        textField = new JTextField();

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            sendCurrentMessage();
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(textField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    void sendCurrentMessage() {
        String text = textField.getText();
        if (text.isEmpty()) {
            return;
        }
        Message message = new Message(text);
        displaySentMessage(message);

        if (clientHandler != null) {
            clientHandler.sendMessage(message);
        }

        textField.setText("");
    }

    void displaySentMessage(Message message) {
        createMessageFrame(message, Color.YELLOW);
    }

    void displayReceivedMessage(Message message) {
        createMessageFrame(message, Color.GREEN);
    }

    private void createMessageFrame(Message message, Color color) {
        JLabel messageFrame = new JLabel(message.getString());

        messageFrame.setOpaque(true);
        messageFrame.setBackground(color);
        messageFrame.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.add(messageFrame);
        messagePanel.add(wrapper);
        messagePanel.revalidate();
        messagePanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) messagePanel.getParent().getParent()).getVerticalScrollBar(); 
            vertical.setValue(vertical.getMaximum());
        });
    }

    void setClientHandler(ClientHandler handler) {
        this.clientHandler = handler;
    }
}
