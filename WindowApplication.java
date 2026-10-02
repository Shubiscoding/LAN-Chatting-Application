import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Main chat UI — WhatsApp/Discord-style split-panel layout.
 *
 * LEFT:  Contact sidebar (profile header, contact list, add contact button)
 * RIGHT: Chat area (header, message bubbles, input bar) or empty state
 *
 * This class manages all UI state: selected contact, chat history,
 * contact list, and message display.
 */
public class WindowApplication {

    // ══════════════════════════════════════════
    //  COLOR PALETTE (matches LoginPage theme)
    // ══════════════════════════════════════════
    private static final Color DARK_BG        = new Color(0x0F, 0x3D, 0x22);    // darkest green - profile header
    private static final Color SIDEBAR_BG     = new Color(0x14, 0x53, 0x2D);    // dark green - sidebar
    private static final Color SIDEBAR_HOVER  = new Color(0x1B, 0x6B, 0x3A);    // lighter green - hover
    private static final Color SIDEBAR_SEL    = new Color(0x16, 0x65, 0x34);    // selected contact
    private static final Color CHAT_BG        = new Color(0xEC, 0xFD, 0xF5);    // very light green - chat area
    private static final Color SENT_BUBBLE    = new Color(0x16, 0x65, 0x34);    // sent message bubble
    private static final Color RECV_BUBBLE    = new Color(0xDC, 0xFC, 0xE7);    // received message bubble
    private static final Color HEADER_BG      = new Color(0x14, 0x53, 0x2D);    // chat header
    private static final Color INPUT_BG       = new Color(0xF0, 0xFD, 0xF4);    // input bar background
    private static final Color WHITE          = Color.WHITE;
    private static final Color TEXT_DARK      = new Color(0x1A, 0x1A, 0x1A);
    private static final Color TEXT_MUTED     = new Color(0x8A, 0xB0, 0x8A);
    private static final Color SEPARATOR      = new Color(0x1B, 0x5E, 0x35);
    private static final Color ACCENT_GREEN   = new Color(0x4A, 0xDE, 0x80);
    private static final Color INPUT_BORDER   = new Color(0xBB, 0xDE, 0xBB);

    // Distinct avatar colors for different contacts
    private static final Color[] AVATAR_COLORS = {
        new Color(0xE5, 0x39, 0x35), new Color(0x43, 0xA0, 0x47),
        new Color(0x1E, 0x88, 0xE5), new Color(0xFB, 0x8C, 0x00),
        new Color(0x8E, 0x24, 0xAA), new Color(0x00, 0x96, 0x88),
        new Color(0xF4, 0x43, 0x36), new Color(0x39, 0x49, 0xAB),
    };

    // ══════════════════════════════════════════
    //  STATE
    // ══════════════════════════════════════════
    private JFrame frame;
    private Profile myProfile;
    private ClientHandler clientHandler;
    private int selectedContactId = -1;
    private ArrayList<Profile> contacts;
    private HashMap<Integer, ArrayList<Message>> chatHistory;

    // ══════════════════════════════════════════
    //  UI COMPONENTS
    // ══════════════════════════════════════════
    private JPanel contactListPanel;
    private JPanel chatMessagesPanel;
    private JScrollPane chatScrollPane;
    private JTextField messageField;
    private JLabel chatHeaderName;
    private JPanel chatHeaderPanel;
    private JPanel rightPanel;
    private JButton sendButton;

    // ══════════════════════════════════════════
    //  CONNECTION
    // ══════════════════════════════════════════
    private Sender sender;
    private Receiver receiver;

    // ══════════════════════════════════════════
    //  CONSTRUCTOR
    // ══════════════════════════════════════════
    WindowApplication(Profile profile, Sender sender, Receiver receiver) {
        this.myProfile = profile;
        this.sender = sender;
        this.receiver = receiver;

        // Load persisted data
        this.contacts = FileManager.LoadContacts();
        this.chatHistory = FileManager.LoadMessages();

        createWindow();
    }

    /**
     * Starts the ClientHandler background thread to listen for incoming messages.
     * Call this after the window is created and visible.
     */
    void start() {
        clientHandler = new ClientHandler(this, sender, receiver);
        Thread thread = new Thread(clientHandler);
        thread.setDaemon(true);
        thread.start();
    }

    // ══════════════════════════════════════════
    //  MAIN WINDOW SETUP
    // ══════════════════════════════════════════
    private void createWindow() {
        frame = new JFrame("LAN Chat \u2014 " + myProfile.getUsername());
        frame.setSize(1050, 680);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(800, 500));

        // Split pane: sidebar (left) | chat area (right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(310);
        splitPane.setDividerSize(1);
        splitPane.setEnabled(false);  // fixed divider — not user-draggable
        splitPane.setBorder(null);

        splitPane.setLeftComponent(createSidebar());
        splitPane.setRightComponent(createRightPanel());

        frame.add(splitPane);
        frame.setVisible(true);
    }

    // ══════════════════════════════════════════
    //  LEFT SIDEBAR
    // ══════════════════════════════════════════
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(310, 680));

        // Top: profile header
        sidebar.add(createProfileHeader(), BorderLayout.NORTH);

        // Center: scrollable contact list
        contactListPanel = new JPanel();
        contactListPanel.setLayout(new BoxLayout(contactListPanel, BoxLayout.Y_AXIS));
        contactListPanel.setBackground(SIDEBAR_BG);
        refreshContactList();

        JScrollPane scrollPane = new JScrollPane(contactListPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(SIDEBAR_BG);
        sidebar.add(scrollPane, BorderLayout.CENTER);

        // Bottom: Add Contact button
        JButton addBtn = new JButton("+ Add Contact");
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addBtn.setBackground(SIDEBAR_SEL);
        addBtn.setForeground(WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.setPreferredSize(new Dimension(310, 45));
        addBtn.addActionListener(e -> showAddContactDialog());
        addBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { addBtn.setBackground(SIDEBAR_HOVER); }
            public void mouseExited(MouseEvent e)  { addBtn.setBackground(SIDEBAR_SEL); }
        });

        sidebar.add(addBtn, BorderLayout.SOUTH);

        return sidebar;
    }

    /**
     * Creates the profile header at the top of the sidebar.
     * Shows the current user's avatar, username, and online status.
     */
    private JPanel createProfileHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_BG);
        header.setPreferredSize(new Dimension(310, 70));
        header.setBorder(new EmptyBorder(12, 15, 12, 15));

        // Avatar circle
        String initial = myProfile.getUsername().isEmpty() ? "?" :
                myProfile.getUsername().substring(0, 1).toUpperCase();
        JPanel avatar = createAvatarCircle(initial, ACCENT_GREEN, 42);

        // Username and status
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(4, 12, 4, 0));

        JLabel nameLabel = new JLabel(myProfile.getUsername());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLabel.setForeground(WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusLabel = new JLabel("\u25CF Online");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(ACCENT_GREEN);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(nameLabel);
        info.add(Box.createRigidArea(new Dimension(0, 3)));
        info.add(statusLabel);

        header.add(avatar, BorderLayout.WEST);
        header.add(info, BorderLayout.CENTER);

        return header;
    }

    // ══════════════════════════════════════════
    //  CONTACT LIST
    // ══════════════════════════════════════════

    /**
     * Rebuilds the contact list panel from the contacts ArrayList.
     * Called when contacts change (add, message received from new user, etc.)
     */
    private void refreshContactList() {
        contactListPanel.removeAll();

        if (contacts.isEmpty()) {
            JLabel emptyLabel = new JLabel("No contacts yet");
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setBorder(new EmptyBorder(40, 0, 0, 0));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel hintLabel = new JLabel("Click '+ Add Contact' below");
            hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            hintLabel.setForeground(TEXT_MUTED);
            hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
            hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            contactListPanel.add(emptyLabel);
            contactListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            contactListPanel.add(hintLabel);
        } else {
            for (Profile contact : contacts) {
                contactListPanel.add(createContactCard(contact));
            }
        }

        contactListPanel.add(Box.createVerticalGlue());
        contactListPanel.revalidate();
        contactListPanel.repaint();
    }

    /**
     * Creates a clickable contact card for the sidebar.
     * Shows avatar circle, username, and last message preview.
     */
    private JPanel createContactCard(Profile contact) {
        boolean isSelected = contact.getId() == selectedContactId;

        JPanel card = new JPanel(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setBackground(isSelected ? SIDEBAR_SEL : SIDEBAR_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, SEPARATOR),
                new EmptyBorder(10, 15, 10, 15)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // --- Avatar ---
        Color avatarColor = AVATAR_COLORS[Math.abs(contact.getId()) % AVATAR_COLORS.length];
        String initial = contact.getUsername().isEmpty() ? "?"
                : contact.getUsername().substring(0, 1).toUpperCase();
        JPanel avatar = createAvatarCircle(initial, avatarColor, 42);

        // Wrapper to vertically center the avatar
        JPanel avatarWrapper = new JPanel(new GridBagLayout());
        avatarWrapper.setOpaque(false);
        avatarWrapper.add(avatar);

        // --- Name + last message ---
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(2, 12, 2, 0));

        JLabel name = new JLabel(contact.getUsername());
        name.setFont(new Font("SansSerif", Font.BOLD, 14));
        name.setForeground(WHITE);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        String lastMsg = getLastMessagePreview(contact.getId());
        JLabel preview = new JLabel(lastMsg);
        preview.setFont(new Font("SansSerif", Font.PLAIN, 12));
        preview.setForeground(TEXT_MUTED);
        preview.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(name);
        textPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        textPanel.add(preview);

        card.add(avatarWrapper, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        // --- Hover & click ---
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectContact(contact.getId());
            }

            public void mouseEntered(MouseEvent e) {
                if (contact.getId() != selectedContactId)
                    card.setBackground(SIDEBAR_HOVER);
            }

            public void mouseExited(MouseEvent e) {
                card.setBackground(contact.getId() == selectedContactId ? SIDEBAR_SEL : SIDEBAR_BG);
            }
        });

        return card;
    }

    /**
     * Returns a truncated preview of the last message with a contact.
     */
    private String getLastMessagePreview(int contactId) {
        ArrayList<Message> msgs = chatHistory.get(contactId);
        if (msgs == null || msgs.isEmpty()) return "No messages yet";
        String last = msgs.get(msgs.size() - 1).getString();
        return last.length() > 30 ? last.substring(0, 30) + "\u2026" : last;
    }

    // ══════════════════════════════════════════
    //  RIGHT PANEL (Empty State + Active Chat)
    // ══════════════════════════════════════════

    /**
     * Creates the right panel using CardLayout to switch between
     * empty state and active chat view.
     */
    private JPanel createRightPanel() {
        rightPanel = new JPanel(new CardLayout());

        // Card 1: Empty state (shown when no contact is selected)
        rightPanel.add(createEmptyState(), "empty");

        // Card 2: Active chat (shown when a contact is selected)
        rightPanel.add(createActiveChatPanel(), "chat");

        ((CardLayout) rightPanel.getLayout()).show(rightPanel, "empty");

        return rightPanel;
    }

    private JPanel createEmptyState() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CHAT_BG);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel icon = new JLabel("\uD83D\uDCAC"); // 💬
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("LAN Chat");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(SIDEBAR_BG);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Select a contact to start chatting");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(icon);
        center.add(Box.createRigidArea(new Dimension(0, 15)));
        center.add(title);
        center.add(Box.createRigidArea(new Dimension(0, 8)));
        center.add(subtitle);

        panel.add(center);
        return panel;
    }

    /**
     * Creates the active chat panel with header, messages, and input bar.
     */
    private JPanel createActiveChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CHAT_BG);

        // --- Chat Header ---
        chatHeaderPanel = new JPanel(new BorderLayout());
        chatHeaderPanel.setBackground(HEADER_BG);
        chatHeaderPanel.setPreferredSize(new Dimension(0, 60));
        chatHeaderPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        chatHeaderName = new JLabel("");
        chatHeaderName.setFont(new Font("SansSerif", Font.BOLD, 16));
        chatHeaderName.setForeground(WHITE);
        chatHeaderPanel.add(chatHeaderName, BorderLayout.CENTER);

        panel.add(chatHeaderPanel, BorderLayout.NORTH);

        // --- Message Display ---
        chatMessagesPanel = new JPanel();
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setBackground(CHAT_BG);
        chatMessagesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        chatScrollPane = new JScrollPane(chatMessagesPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.getViewport().setBackground(CHAT_BG);

        panel.add(chatScrollPane, BorderLayout.CENTER);

        // --- Input Bar ---
        panel.add(createInputBar(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the message input bar with text field and send button.
     */
    private JPanel createInputBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(INPUT_BG);
        bar.setBorder(new EmptyBorder(10, 15, 10, 15));
        bar.setPreferredSize(new Dimension(0, 55));

        // Text field
        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(INPUT_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        messageField.addActionListener(e -> sendCurrentMessage());

        // Send button
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        sendButton.setBackground(SIDEBAR_SEL);
        sendButton.setForeground(WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setPreferredSize(new Dimension(80, 35));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> sendCurrentMessage());
        sendButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { sendButton.setBackground(SIDEBAR_HOVER); }
            public void mouseExited(MouseEvent e)  { sendButton.setBackground(SIDEBAR_SEL); }
        });

        bar.add(messageField, BorderLayout.CENTER);
        bar.add(sendButton, BorderLayout.EAST);

        return bar;
    }

    // ══════════════════════════════════════════
    //  CONTACT SELECTION
    // ══════════════════════════════════════════

    /**
     * Selects a contact: updates the header, loads their chat history,
     * switches the right panel to the chat view, and highlights the contact.
     */
    private void selectContact(int contactId) {
        selectedContactId = contactId;

        // Update chat header
        Profile contact = findContact(contactId);
        if (contact != null) {
            chatHeaderPanel.removeAll();

            // Avatar in header
            Color avatarColor = AVATAR_COLORS[Math.abs(contactId) % AVATAR_COLORS.length];
            String initial = contact.getUsername().isEmpty() ? "?"
                    : contact.getUsername().substring(0, 1).toUpperCase();
            JPanel avatar = createAvatarCircle(initial, avatarColor, 36);

            // Name label
            JLabel nameLabel = new JLabel("  " + contact.getUsername());
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            nameLabel.setForeground(WHITE);

            JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            headerLeft.setOpaque(false);
            headerLeft.add(avatar);
            headerLeft.add(nameLabel);

            chatHeaderPanel.add(headerLeft, BorderLayout.WEST);
            chatHeaderPanel.revalidate();
            chatHeaderPanel.repaint();
        }

        // Load and display messages
        loadChatMessages(contactId);

        // Switch to chat view
        ((CardLayout) rightPanel.getLayout()).show(rightPanel, "chat");

        // Refresh sidebar to update selection highlighting
        refreshContactList();

        // Focus the message input
        messageField.requestFocusInWindow();
    }

    /**
     * Loads and displays all messages for the given contact.
     */
    private void loadChatMessages(int contactId) {
        chatMessagesPanel.removeAll();

        ArrayList<Message> messages = chatHistory.get(contactId);
        if (messages != null) {
            for (Message msg : messages) {
                boolean isSent = msg.getSenderId() == myProfile.getId();
                addMessageBubble(msg.getString(), isSent);
            }
        }

        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    // ══════════════════════════════════════════
    //  SENDING MESSAGES
    // ══════════════════════════════════════════

    /**
     * Sends the current text in the message field to the selected contact.
     */
    private void sendCurrentMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || selectedContactId == -1) return;

        Message message = new Message(text, myProfile.getId(), selectedContactId);

        // Send to server for routing
        if (clientHandler != null) {
            clientHandler.sendMessage(message);
        }

        // Save locally
        if (!chatHistory.containsKey(selectedContactId)) {
            chatHistory.put(selectedContactId, new ArrayList<>());
        }
        chatHistory.get(selectedContactId).add(message);
        FileManager.WriteMessage(selectedContactId, message);

        // Display the sent message
        addMessageBubble(text, true);
        messageField.setText("");

        // Update last message preview in sidebar
        refreshContactList();

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    // ══════════════════════════════════════════
    //  RECEIVING MESSAGES (called by ClientHandler)
    // ══════════════════════════════════════════

    /**
     * Called when a chat message is received from another user.
     * Saves to history, displays if the sender is the active contact,
     * and updates the sidebar.
     */
    void onMessageReceived(Message message) {
        int senderId = message.getSenderId();

        // Save to chat history
        if (!chatHistory.containsKey(senderId)) {
            chatHistory.put(senderId, new ArrayList<>());
        }
        chatHistory.get(senderId).add(message);
        FileManager.WriteMessage(senderId, message);

        // If we're currently viewing this contact's chat, display the message
        if (senderId == selectedContactId) {
            addMessageBubble(message.getString(), false);

            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        }

        // Auto-add sender if not in contacts
        if (findContact(senderId) == null) {
            Profile newContact = new Profile(senderId, "User " + senderId, "");
            contacts.add(newContact);
            FileManager.WriteContacts(contacts);
        }

        // Refresh sidebar to show updated last message
        refreshContactList();
    }

    /**
     * Called when the server responds to a FIND_USER request with a match.
     * Adds the found user to the contact list.
     */
    void onContactFound(Profile contact) {
        if (findContact(contact.getId()) != null) {
            JOptionPane.showMessageDialog(frame,
                    contact.getUsername() + " is already in your contacts.",
                    "Contact Exists", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        contacts.add(contact);
        FileManager.WriteContacts(contacts);
        refreshContactList();

        JOptionPane.showMessageDialog(frame,
                "Added " + contact.getUsername() + " to your contacts!",
                "Contact Added", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Called when the server responds to a FIND_USER request with no match.
     */
    void onContactNotFound() {
        JOptionPane.showMessageDialog(frame,
                "User not found. Please check the User ID.",
                "Not Found", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Called when a sent message couldn't be delivered because the recipient is offline.
     */
    void onUserOffline(int userId) {
        Profile contact = findContact(userId);
        String name = contact != null ? contact.getUsername() : "User " + userId;
        JOptionPane.showMessageDialog(frame,
                name + " is currently offline. Your message was not delivered.",
                "User Offline", JOptionPane.INFORMATION_MESSAGE);
    }

    // ══════════════════════════════════════════
    //  ADD CONTACT DIALOG
    // ══════════════════════════════════════════

    /**
     * Shows a dialog to add a new contact by User ID.
     * Sends a FIND_USER protocol message to the server.
     */
    private void showAddContactDialog() {
        String input = JOptionPane.showInputDialog(frame,
                "Enter the User ID of the contact to add:",
                "Add Contact", JOptionPane.PLAIN_MESSAGE);

        if (input == null || input.trim().isEmpty()) return;

        try {
            int targetId = Integer.parseInt(input.trim());

            if (targetId == myProfile.getId()) {
                JOptionPane.showMessageDialog(frame,
                        "You can't add yourself as a contact!",
                        "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (findContact(targetId) != null) {
                JOptionPane.showMessageDialog(frame,
                        "This user is already in your contacts.",
                        "Already Added", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Ask the server to look up this user
            if (clientHandler != null) {
                clientHandler.sendProtocolMessage("FIND_USER:" + targetId);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame,
                    "Please enter a valid number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════
    //  AVATAR CIRCLE (custom painted component)
    // ══════════════════════════════════════════

    /**
     * Creates a circular avatar with a letter initial and background color.
     * Uses custom painting for anti-aliased circles.
     */
    private JPanel createAvatarCircle(String letter, Color bgColor, int size) {
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw circle (always square, even if panel is stretched)
                int diameter = Math.min(getWidth(), getHeight());
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;
                g2.setColor(bgColor);
                g2.fillOval(x, y, diameter, diameter);

                // Draw centered letter
                g2.setColor(WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, diameter / 2));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(letter)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(letter, tx, ty);

                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(size, size));
        avatar.setMinimumSize(new Dimension(size, size));
        avatar.setMaximumSize(new Dimension(size, size));
        avatar.setOpaque(false);
        return avatar;
    }

    // ══════════════════════════════════════════
    //  MESSAGE BUBBLE (custom painted component)
    // ══════════════════════════════════════════

    /**
     * Adds a chat bubble to the messages panel.
     * Sent messages are right-aligned with green background.
     * Received messages are left-aligned with light background.
     */
    private void addMessageBubble(String text, boolean isSent) {
        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSent ? SENT_BUBBLE : RECV_BUBBLE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setLayout(new BorderLayout());

        // Use HTML to enable word wrapping within a fixed width
        JLabel label = new JLabel(
                "<html><body style='width:250px; padding:2px;'>"
                        + escapeHtml(text) + "</body></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setForeground(isSent ? WHITE : TEXT_DARK);
        label.setBorder(new EmptyBorder(8, 14, 8, 14));
        bubble.add(label);

        bubble.setMaximumSize(new Dimension(380, Integer.MAX_VALUE));

        // Wrapper for alignment (right for sent, left for received)
        JPanel wrapper = new JPanel(
                new FlowLayout(isSent ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 3));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Short.MAX_VALUE));
        wrapper.add(bubble);

        chatMessagesPanel.add(wrapper);
        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();
    }

    /**
     * Escapes HTML special characters in message text.
     */
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

    // ══════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════

    /**
     * Finds a contact Profile by ID.
     */
    private Profile findContact(int id) {
        for (Profile p : contacts) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    /**
     * Legacy setter — kept for backward compatibility.
     */
    void setClientHandler(ClientHandler handler) {
        this.clientHandler = handler;
    }
}
