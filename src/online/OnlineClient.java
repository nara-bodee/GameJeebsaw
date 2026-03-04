package online;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;

public class OnlineClient {

    private volatile boolean intentionalDisconnect = false;
    private static final String RECONNECT_FILE = "reconnect.dat";

    // ==========================================
    // Legacy Reconnect Data Management (From HEAD)
    // ==========================================
    private void saveReconnectData(String ip, int port, String token, String name) {
        try (PrintWriter out = new PrintWriter(new FileWriter(RECONNECT_FILE))) {
            out.println(ip + "," + port + "," + token + "," + name);
        } catch (IOException ignored) {}
    }

    public static String[] loadReconnectData() {
        File f = new File(RECONNECT_FILE);
        if (!f.exists()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            if (line != null) {
                return line.split(","); // [ip, port, token, name]
            }
        } catch (IOException ignored) {}
        return null;
    }

    public static void clearReconnectData() {
        new File(RECONNECT_FILE).delete();
    }

    // ==========================================
    // Listener Interface
    // ==========================================
    public interface ClientListener {
        void onConnected(String playerId, String roomName, int maxPlayers, int currentPlayers);
        void onPlayerListChanged(List<String> players);
        void onStartGame();
        void onScoreboard(String scoreboardText);
        void onStateSync(String state);
        void onError(String error);
        void onDisconnected();
        void onRole(String role);
        void onReadyStatus(String status);
        void onAllReady();
    }

    private final String hostAddress;
    private final int port;
    private final String requestedName;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean running;

    private String sessionToken; // ใช้ sessionToken เป็นหลัก
    private ClientListener listener;

    public OnlineClient(String hostAddress, int port, String requestedName) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.requestedName = sanitize(requestedName);
    }

    // ==========================================
    // Getters
    // ==========================================
    public int getPort() {
        return port;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    // ==========================================
    // Connection Methods
    // ==========================================
    public void connect() throws IOException {
        socket = new Socket(hostAddress, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        running = true;
        intentionalDisconnect = false;

        writer.println("HELLO|" + requestedName);

        Thread readerThread = new Thread(this::readLoop, "OnlineClient-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    /**
     * Establishes a new connection to the server to attempt a reconnect using a session token.
     * @param token The session token for reconnection.
     * @throws IOException if a connection error occurs.
     */
    public void reconnect(String token) throws IOException {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty for reconnect.");
        }
        
        socket = new Socket(hostAddress, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        running = true;
        intentionalDisconnect = false;
        
        this.sessionToken = token;
        writer.println("RECONNECT|" + token);

        Thread readerThread = new Thread(this::readLoop, "OnlineClient-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    // Alias for old HEAD compatibility
    public void connectWithToken(String token) throws IOException {
        reconnect(token);
    }

    // ==========================================
    // Client Actions
    // ==========================================
    public void sendScore(int score) {
        if (writer != null) {
            writer.println("SCORE|" + score);
        }
    }

    public void sendReady() {
        if (writer != null) writer.println("READY");
    }

    public void sendUnready() {
        if (writer != null) writer.println("UNREADY");
    }

    public void requestStart() {
        if (writer != null) writer.println("START_REQUEST");
    }

    public void disconnect() {
        intentionalDisconnect = true; 
        running = false;
        if (writer != null) {
            writer.println("QUIT");
        }
        closeSocket();
    }

    // ==========================================
    // Read Loop
    // ==========================================
    private void readLoop() {
        try {
            String line;

            while (running && (line = reader.readLine()) != null) {

                if (line.startsWith("WELCOME|")) { // Server Protocol from HEAD
                    String[] parts = line.split("\\|");
                    if (parts.length >= 6) {
                        String playerId = parts[1];
                        sessionToken = parts[2];
                        String roomName = parts[3];
                        int maxPlayers = parseInt(parts[4], 3);
                        int currentPlayers = parseInt(parts[5], 1);
                        saveReconnectData(hostAddress, port, sessionToken, requestedName);
                        notifyConnected(playerId, roomName, maxPlayers, currentPlayers);
                    }
                } else if (line.startsWith("WELCOME_TOKEN|")) { // Server Protocol from add_Token_DevMax
                    String[] parts = line.split("\\|", 4);
                    if (parts.length >= 4) {
                        String assignedName = parts[1];
                        String roomName = parts[2];
                        sessionToken = parts[3];
                        notifyConnected(assignedName, roomName, 0, 0); 
                    }

                } else if (line.startsWith("PLAYER_LIST|")) {
                    String payload = line.substring("PLAYER_LIST|".length()).trim();
                    List<String> players = new ArrayList<>();
                    if (!payload.isEmpty()) {
                        String[] parts = payload.split(",");
                        for (String p : parts) {
                            players.add(p.trim());
                        }
                    }
                    notifyPlayerList(players);

                } else if (line.equals("START")) {
                    notifyStart();

                } else if (line.startsWith("SCOREBOARD|")) {
                    String board = line.substring("SCOREBOARD|".length()).replace("\\n", "\n");
                    notifyScoreboard(board);

                } else if (line.startsWith("STATE_SYNC|")) {
                    String state = line.substring("STATE_SYNC|".length());
                    notifyStateSync(state);

                } else if (line.startsWith("ERROR|")) {
                    notifyError(line.substring("ERROR|".length()));
                    disconnect();

                } else if (line.startsWith("ROLE|")) {
                    notifyRole(line.split("\\|")[1]);

                } else if (line.startsWith("READY_STATUS|")) {
                    notifyReadyStatus(line.substring("READY_STATUS|".length()));

                } else if (line.equals("ALL_READY")) {
                    notifyAllReady();
                }
            }

        } catch (IOException e) {
            if (running) {
                notifyError("การเชื่อมต่อหลุด: " + e.getMessage());
            }
        } finally {
            running = false;
            closeSocket();
            if (!intentionalDisconnect) {
                notifyDisconnected();
            }
        }
    }

    // ==========================================
    // Notification Dispatchers
    // ==========================================
    private void notifyConnected(String playerId, String roomName, int maxPlayers, int currentPlayers) {
        if (listener != null) {
            SwingUtilities.invokeLater(() ->
                listener.onConnected(playerId, roomName, maxPlayers, currentPlayers));
        }
    }

    private void notifyPlayerList(List<String> players) {
        if (listener != null) {
            SwingUtilities.invokeLater(() ->
                listener.onPlayerListChanged(players));
        }
    }

    private void notifyStart() {
        if (listener != null) {
            SwingUtilities.invokeLater(listener::onStartGame);
        }
    }

    private void notifyScoreboard(String board) {
        if (listener != null) {
            SwingUtilities.invokeLater(() ->
                listener.onScoreboard(board));
        }
    }

    private void notifyStateSync(String state) {
        if (listener != null) {
            SwingUtilities.invokeLater(() ->
                listener.onStateSync(state));
        }
    }

    private void notifyError(String error) {
        if (listener != null) {
            SwingUtilities.invokeLater(() ->
                listener.onError(error));
        }
    }

    private void notifyDisconnected() {
        if (listener != null) {
            SwingUtilities.invokeLater(listener::onDisconnected);
        }
    }

    private void notifyRole(String role) {
        if (listener != null) {
            SwingUtilities.invokeLater(() ->
                listener.onRole(role));
        }
    }

    private void notifyReadyStatus(String status) {
        if (listener != null) {
            SwingUtilities.invokeLater(() ->
                listener.onReadyStatus(status));
        }
    }

    private void notifyAllReady() {
        if (listener != null) {
            SwingUtilities.invokeLater(listener::onAllReady);
        }
    }

    // ==========================================
    // Utils
    // ==========================================
    private void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private static String sanitize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Player";
        }
        return text.trim()
                   .replace("|", "_")
                   .replace(",", "_")
                   .replace(";", "_")
                   .replace(":", "_");
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}