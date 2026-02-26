package online;

import java.io.BufferedReader;
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

    public interface ClientListener {
        void onConnected(String assignedName, String roomName, int maxPlayers, int currentPlayers);
        void onPlayerListChanged(List<String> players);
        void onStartGame();
        void onScoreboard(String scoreboardText);
        void onError(String error);
        void onDisconnected();
    }

    private final String hostAddress;
    private final int port;
    private final String requestedName;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean running;
    private ClientListener listener;

    public OnlineClient(String hostAddress, int port, String requestedName) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.requestedName = sanitize(requestedName);
    }

    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    public void connect() throws IOException {
        socket = new Socket(hostAddress, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        running = true;

        writer.println("JOIN|" + requestedName);

        Thread readerThread = new Thread(this::readLoop, "OnlineClient-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void sendScore(int score) {
        if (writer != null) {
            writer.println("SCORE|" + score);
        }
    }

    public void disconnect() {
        running = false;
        if (writer != null) {
            writer.println("QUIT");
        }
        closeSocket();
    }

    private void readLoop() {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                if (line.startsWith("WELCOME|")) {
                    String[] parts = line.split("\\|", 5);
                    if (parts.length >= 5) {
                        String assignedName = parts[1];
                        String roomName = parts[2];
                        int maxPlayers = parseInt(parts[3], 3);
                        int currentPlayers = parseInt(parts[4], 1);
                        notifyConnected(assignedName, roomName, maxPlayers, currentPlayers);
                    }
                } else if (line.startsWith("PLAYER_LIST|")) {
                    String payload = line.substring("PLAYER_LIST|".length());
                    List<String> players = payload.isEmpty()
                        ? new ArrayList<>()
                        : new ArrayList<>(Arrays.asList(payload.split(",")));
                    notifyPlayerList(players);
                } else if (line.equals("START")) {
                    notifyStart();
                } else if (line.startsWith("SCOREBOARD|")) {
                    String board = line.substring("SCOREBOARD|".length()).replace("\\n", "\n");
                    notifyScoreboard(board);
                } else if (line.startsWith("ERROR|")) {
                    notifyError(line.substring("ERROR|".length()));
                }
            }
        } catch (IOException e) {
            if (running) {
                notifyError("การเชื่อมต่อหลุด: " + e.getMessage());
            }
        } finally {
            running = false;
            closeSocket();
            notifyDisconnected();
        }
    }

    private void notifyConnected(String assignedName, String roomName, int maxPlayers, int currentPlayers) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onConnected(assignedName, roomName, maxPlayers, currentPlayers));
        }
    }

    private void notifyPlayerList(List<String> players) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onPlayerListChanged(players));
        }
    }

    private void notifyStart() {
        if (listener != null) {
            SwingUtilities.invokeLater(listener::onStartGame);
        }
    }

    private void notifyScoreboard(String board) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onScoreboard(board));
        }
    }

    private void notifyError(String error) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onError(error));
        }
    }

    private void notifyDisconnected() {
        if (listener != null) {
            SwingUtilities.invokeLater(listener::onDisconnected);
        }
    }

    private void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static String sanitize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Player";
        }
        return text.trim().replace("|", "_").replace(",", "_").replace(";", "_").replace(":", "_");
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
