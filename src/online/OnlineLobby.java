package online;

import main.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * An example lobby screen that demonstrates auto-filling the player name
 * and handling the connection lifecycle, including reconnection.
 */
public class OnlineLobby extends JFrame implements OnlineClient.ClientListener {

    private final JTextField nameField;
    private final JTextField hostField;
    private final JButton connectButton;
    private final JTextArea logArea;

    private OnlineClient onlineClient;
    private GameWindow gameWindow;

    public OnlineLobby() {
        setTitle("Game Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- UI Components ---
        JPanel topPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Player Name Field (auto-filled from config)
        nameField = new JTextField(PlayerConfigManager.loadLastPlayerName());
        topPanel.add(new JLabel("Your Name:"));
        topPanel.add(nameField);

        // Host Address Field
        hostField = new JTextField("localhost"); // Default for local testing
        topPanel.add(new JLabel("Server Address:"));
        topPanel.add(hostField);

        connectButton = new JButton("Connect");
        topPanel.add(new JLabel()); // Spacer
        topPanel.add(connectButton);

        logArea = new JTextArea("Welcome! Enter your name and connect to a server.\n");
        logArea.setEditable(false);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        connectButton.addActionListener(e -> connectToServer());
    }

    private void connectToServer() {
        String playerName = nameField.getText().trim();
        String host = hostField.getText().trim();
        // In a real game, you'd get the port from LanDiscovery or user input.
        // For this example, we assume the server is running on the discovery port.
        int port = LanDiscovery.DISCOVERY_PORT;

        if (playerName.isEmpty()) {
            log("Please enter a name.");
            return;
        }

        try {
            log("Connecting to " + host + "...");
            onlineClient = new OnlineClient(host, port, playerName);
            onlineClient.setListener(this);
            onlineClient.connect();
            connectButton.setEnabled(false);
            connectButton.setText("Connecting...");
        } catch (IOException ex) {
            log("Error: " + ex.getMessage());
            connectButton.setEnabled(true);
            connectButton.setText("Connect");
        }
    }

    private void attemptReconnect() {
        String token = ClientTokenManager.loadToken();
        if (token == null || token.isEmpty()) {
            log("No session token found. Cannot reconnect.");
            // If reconnect is impossible, reset the button and return to lobby
            if (gameWindow != null) {
                gameWindow.setVisible(false);
                this.setVisible(true);
            }
            return;
        }

        log("Attempting to reconnect with token...");
        try {
            onlineClient = new OnlineClient(hostField.getText().trim(), onlineClient.getPort(), "");
            onlineClient.setListener(this);
            onlineClient.reconnect(token);
        } catch (IOException | NullPointerException ex) {
            log("Reconnect failed: " + ex.getMessage());
            if (gameWindow != null) {
                gameWindow.setConnectionState(true); // Re-enable the button
            }
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    // --- OnlineClient.ClientListener Implementation ---

    @Override
    public void onConnected(String assignedName, String roomName, int maxPlayers, int currentPlayers) {
        log("Connected successfully as: " + assignedName);

        if (gameWindow == null) { // First-time connection
            PlayerConfigManager.saveLastPlayerName(assignedName);
            this.setVisible(false);

            IntConsumer scoreHandler = score -> log("Final score received: " + score);
            Runnable reconnectHandler = this::attemptReconnect;

            gameWindow = new GameWindow(assignedName, scoreHandler, reconnectHandler);
            gameWindow.setVisible(true);
        } else { // Reconnected
            gameWindow.setConnectionState(false); // Hide reconnect button
            log("Reconnected. Waiting for server to sync game state...");
        }
    }

    @Override
    public void onDisconnected() {
        log("Disconnected from server.");
        if (gameWindow != null && gameWindow.isVisible()) {
            SwingUtilities.invokeLater(() -> gameWindow.setConnectionState(true));
        } else {
            connectButton.setEnabled(true);
            connectButton.setText("Connect");
        }
    }

    @Override
    public void onError(String error) {
        log("Server Error: " + error);
        onDisconnected(); // Treat errors as a disconnection
        if ("INVALID_TOKEN".equals(error)) {
            ClientTokenManager.deleteToken();
            log("Your session token was invalid and has been deleted.");
        }
    }

    @Override public void onPlayerListChanged(List<String> players) { log("Players: " + players); }
    @Override public void onStartGame() { log("Game is starting!"); }
    @Override public void onScoreboard(String scoreboardText) { log("--- Scoreboard ---\n" + scoreboardText); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OnlineLobby().setVisible(true));
    }
    @Override
    public void onStateSync(String state) {
        // ยังไม่ต้องทำอะไร
    }

    @Override
    public void onAllReady() {
        // ยังไม่ต้องทำอะไร
    }

    @Override
    public void onReadyStatus(String status) {
        // ยังไม่ต้องทำอะไร
    }

    @Override
    public void onRole(String role) {
        // ยังไม่ต้องทำอะไร
    }
}