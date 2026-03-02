package online;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.SwingUtilities;

public class OnlineServer {

    public interface ServerListener {
        void onPlayerListChanged(List<String> players);
        void onScoreboardReady(String scoreboardText);
        void onError(String error);
    }

    private record PlayerState(String playerName, String sessionToken, int score) {}

    private final String roomName;
    private final String hostName;
    private final int maxPlayers;

    private final Map<String, PlayerState> disconnectedPlayers = new ConcurrentHashMap<>(); // Key: token
    private final Set<String> players = new LinkedHashSet<>();
    private final List<ClientSession> sessions = new CopyOnWriteArrayList<>();
    private final Map<String, Integer> scores = new LinkedHashMap<>();

    private ServerSocket serverSocket;
    private DatagramSocket discoverySocket;
    private Thread acceptThread;
    private Thread discoveryThread;

    private volatile boolean running;
    private volatile boolean gameStarted;
    private volatile boolean scoreboardSent;
    private int expectedPlayersAtStart;
    private ServerListener listener;

    public OnlineServer(String roomName, String hostName, int maxPlayers) {
        this.roomName = sanitize(roomName);
        this.hostName = sanitize(hostName);
        this.maxPlayers = maxPlayers;
        this.players.add(this.hostName);
    }

    public synchronized void setListener(ServerListener listener) {
        this.listener = listener;
    }

    public synchronized void start() throws IOException {
        serverSocket = new ServerSocket(0);
        running = true;
        startAcceptLoop();
        startDiscoveryLoop();
        notifyPlayerListChanged();
    }

    public synchronized int getPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : -1;
    }

    public synchronized String getRoomName() {
        return roomName;
    }

    public synchronized void startGame() {
        if (!running || gameStarted) {
            return;
        }
        gameStarted = true;
        expectedPlayersAtStart = players.size();
        broadcast("START");
    }

    public synchronized void submitHostScore(int score) {
        submitScore(hostName, score);
    }

    public synchronized void stop() {
        running = false;
        tryClose(serverSocket);
        tryClose(discoverySocket);
        for (ClientSession session : sessions) {
            session.close();
        }
        sessions.clear();
    }

    private void startAcceptLoop() {
        acceptThread = new Thread(() -> {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    if (!running) {
                        tryClose(socket);
                        break;
                    }

                    if (players.size() >= maxPlayers) {
                        rejectClient(socket, "Room is full");
                        continue;
                    }

                    ClientSession session = new ClientSession(socket);
                    sessions.add(session);
                    session.start();
                } catch (IOException e) {
                    if (running) {
                        notifyError("เกิดข้อผิดพลาดเซิร์ฟเวอร์: " + e.getMessage());
                    }
                }
            }
        }, "OnlineServer-Accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    private void startDiscoveryLoop() {
        discoveryThread = new Thread(() -> {
            try {
                discoverySocket = new DatagramSocket(LanDiscovery.DISCOVERY_PORT);
                discoverySocket.setBroadcast(true);
                byte[] buffer = new byte[256];

                while (running) {
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    discoverySocket.receive(request);

                    String message = new String(request.getData(), 0, request.getLength(), StandardCharsets.UTF_8);
                    if (!"GJ_DISCOVER".equals(message)) {
                        continue;
                    }

                    String responseText = "GJ_ROOM|" + roomName + "|" + getPort() + "|" + players.size() + "|" + maxPlayers + "|" + hostName;
                    byte[] responseData = responseText.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket response = new DatagramPacket(
                        responseData,
                        responseData.length,
                        request.getAddress(),
                        request.getPort()
                    );
                    discoverySocket.send(response);
                }
            } catch (IOException e) {
                if (running) {
                    notifyError("Discovery error: " + e.getMessage());
                }
            }
        }, "OnlineServer-Discovery");
        discoveryThread.setDaemon(true);
        discoveryThread.start();
    }

    private synchronized void broadcast(String message) {
        for (ClientSession session : sessions) {
            session.send(message);
        }
    }

    private synchronized void broadcastPlayerList() {
        String joined = String.join(",", players);
        broadcast("PLAYER_LIST|" + joined);
        notifyPlayerListChanged();
    }

    private synchronized void notifyPlayerListChanged() {
        if (listener != null) {
            List<String> snapshot = new ArrayList<>(players);
            SwingUtilities.invokeLater(() -> listener.onPlayerListChanged(snapshot));
        }
    }

    private synchronized void submitScore(String playerName, int score) {
        if (!gameStarted || scoreboardSent) {
            return;
        }

        scores.put(playerName, score);
        if (scores.size() >= expectedPlayersAtStart) {
            scoreboardSent = true;
            String board = buildScoreboard();
            broadcast("SCOREBOARD|" + board.replace("\n", "\\n"));
            if (listener != null) {
                SwingUtilities.invokeLater(() -> listener.onScoreboardReady(board));
            }
        }
    }

    private synchronized String buildScoreboard() {
        List<Map.Entry<String, Integer>> ranking = new ArrayList<>(scores.entrySet());
        ranking.sort(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed());

        StringBuilder builder = new StringBuilder();
        builder.append("ผลคะแนนห้องออนไลน์\n");
        for (int i = 0; i < ranking.size(); i++) {
            Map.Entry<String, Integer> row = ranking.get(i);
            builder.append(i + 1)
                .append(") ")
                .append(row.getKey())
                .append(" - ")
                .append(row.getValue())
                .append("\n");
        }
        return builder.toString().trim();
    }

    private void rejectClient(Socket socket, String message) {
        try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            writer.println("ERROR|" + sanitize(message));
        } catch (IOException ignored) {
        } finally {
            tryClose(socket);
        }
    }

    private void notifyError(String error) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onError(error));
        }
    }

    private static String sanitize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Player";
        }
        return text.trim().replace("|", "_").replace(",", "_").replace(";", "_").replace(":", "_");
    }

    private static void tryClose(ServerSocket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void tryClose(DatagramSocket socket) {
        if (socket != null) {
            socket.close();
        }
    }

    private static void tryClose(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private class ClientSession {
        private final Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String playerName;
        private String sessionToken = UUID.randomUUID().toString();

        ClientSession(Socket socket) {
            this.socket = socket;
        }

        void start() {
            Thread thread = new Thread(this::run, "OnlineServer-Client");
            thread.setDaemon(true);
            thread.start();
        }

        void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

                final String firstLine = reader.readLine();
                if (firstLine == null) {
                    return;
                }

                if (firstLine.startsWith("RECONNECT|")) {
                    String token = firstLine.substring("RECONNECT|".length());
                    PlayerState reconnectedState = disconnectedPlayers.remove(token);

                    if (reconnectedState == null) {
                        send("ERROR|INVALID_TOKEN");
                        return;
                    }

                    synchronized (OnlineServer.this) {
                        this.playerName = reconnectedState.playerName();
                        this.sessionToken = reconnectedState.sessionToken();
                        players.add(this.playerName);
                        scores.put(this.playerName, reconnectedState.score());
                    }

                } else if (firstLine.startsWith("JOIN|")) {
                    if (gameStarted) {
                        send("ERROR|Game has already started");
                        return;
                    }
                    String requestedName = firstLine.substring("JOIN|".length());
                    synchronized (OnlineServer.this) {
                        playerName = uniqueName(sanitize(requestedName));
                        players.add(playerName);
                    }
                } else {
                    send("ERROR|Invalid command");
                    return;
                }

                // Send welcome and current state
                send("WELCOME_TOKEN|" + playerName + "|" + roomName + "|" + sessionToken);
                broadcastPlayerList();

                // If game is in progress, send the start signal to the reconnected client
                if (gameStarted) {
                    send("START");
                }

                String line;
                while (running && (line = reader.readLine()) != null) {
                    if (line.startsWith("SCORE|")) {
                        int score = parseInt(line.substring("SCORE|".length()), 0);
                        synchronized (OnlineServer.this) {
                            submitScore(playerName, score);
                        }
                    } else if (line.equals("QUIT")) {
                        break;
                    }
                }
            } catch (IOException ignored) {
            } finally {
                close();
                synchronized (OnlineServer.this) {
                    sessions.remove(this);
                    if (playerName != null) {
                        if (gameStarted && !scoreboardSent) {
                            // Player disconnected mid-game, save state for reconnect
                            int currentScore = scores.getOrDefault(playerName, 0);
                            disconnectedPlayers.put(sessionToken, new PlayerState(playerName, sessionToken, currentScore));
                            players.remove(playerName); // Remove from active list
                            broadcastPlayerList();
                        } else {
                            // Game not started or already finished, just remove them
                            players.remove(playerName);
                            scores.remove(playerName);
                            if (!gameStarted) {
                                broadcastPlayerList();
                            }
                        }
                    }
                }
            }
        }

        void send(String message) {
            if (writer != null) {
                writer.println(message);
            }
        }

        void close() {
            tryClose(socket);
        }
    }

    private synchronized String uniqueName(String baseName) {
        if (!players.contains(baseName)) {
            return baseName;
        }
        int index = 2;
        while (players.contains(baseName + "_" + index)) {
            index++;
        }
        return baseName + "_" + index;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
