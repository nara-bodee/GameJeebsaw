package online;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;

public class OnlineServer {
    // =========================
    // Turn System
    // =========================
    private final List<String> turnOrder = new ArrayList<>();
    private int currentTurnIndex = 0;

    public interface ServerListener {
        void onPlayerListChanged(List<String> players);
        void onScoreboardReady(String scoreboardText);
        void onError(String error);
    }

    private static final long GRACE_PERIOD_MS = 60000;

    private final String roomName;
    private final String hostName;
    private final int maxPlayers;

    private final Map<String, PlayerSlot> playersByToken = new LinkedHashMap<>();
    private final List<ClientSession> sessions = new CopyOnWriteArrayList<>();

    private ServerSocket serverSocket;
    private DatagramSocket discoverySocket;
    private volatile boolean running;
    private volatile boolean gameStarted;
    private volatile boolean scoreboardSent;

    private int expectedPlayersAtStart;
    private ServerListener listener;

    // =========================
    // Constructor
    // =========================
    public OnlineServer(String roomName, String hostName, int maxPlayers) {
        this.roomName = sanitize(roomName);
        this.hostName = sanitize(hostName);
        this.maxPlayers = maxPlayers;
    }

    // =========================
    // Public API
    // =========================

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    public synchronized void start() throws IOException {
        serverSocket = new ServerSocket(0);
        running = true;
        startAcceptLoop();
        startDiscoveryLoop();
        startCleanupLoop();
        notifyPlayerList();
    }

    public int getPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : -1;
    }

    public String getRoomName() {
        return roomName;
    }

    public synchronized void startGame() {
        if (gameStarted) return;

        gameStarted = true;
        expectedPlayersAtStart = playersByToken.size();
        for (PlayerSlot p : playersByToken.values()) {
            p.score = 0;
            p.scoreSubmitted = false;
        }
        currentTurnIndex = 0;
        broadcast("START");
        broadcastCurrentTurn();
    }

    private void broadcastCurrentTurn() {
        if (turnOrder.isEmpty()) return;

        String token = turnOrder.get(currentTurnIndex);
        PlayerSlot p = playersByToken.get(token);

        if (p != null) {
            broadcast("CURRENT_TURN|" + p.name);
        }
    }

    private synchronized void nextTurn() {
        if (turnOrder.isEmpty()) return;

        int attempts = 0;
        do {
            currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
            String token = turnOrder.get(currentTurnIndex);
            PlayerSlot p = playersByToken.get(token);

            if (p != null && p.connected) {
                broadcastCurrentTurn();
                return;
            }
            attempts++;
        } while (attempts < turnOrder.size());

        System.out.println("No active players.");
    }

    public synchronized void submitHostScore(int score) {
        PlayerSlot host = playersByToken.get("HOST");
        if (host != null) {
            host.score = score;
            checkScoreboard();
        }
    }

    public synchronized void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}
        try { if (discoverySocket != null) discoverySocket.close(); } catch (Exception ignored) {}
        for (ClientSession s : sessions) s.close();
        sessions.clear();
    }

    // =========================
    // Player Model
    // =========================

    private static class PlayerSlot {
        String playerId;
        String name;
        String token;
        boolean connected;
        long disconnectTime;
        int score;
        boolean ready;         
        boolean isHost; 
        ClientSession session;
        boolean scoreSubmitted;
    }

    // =========================
    // Network Loops
    // =========================

    private void startAcceptLoop() {
        Thread acceptThread = new Thread(() -> {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientSession session = new ClientSession(socket);
                    sessions.add(session);
                    session.start();
                } catch (IOException ignored) {}
            }
        }, "OnlineServer-Accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    private void startDiscoveryLoop() {
        Thread discoveryThread = new Thread(() -> {
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

                    long activePlayersCount = playersByToken.values().stream().filter(p -> p.connected).count();
                    String responseText = "GJ_ROOM|" + roomName + "|" + getPort() + "|" + activePlayersCount + "|" + maxPlayers + "|" + hostName;
                    
                    byte[] responseData = responseText.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket response = new DatagramPacket(
                        responseData, responseData.length,
                        request.getAddress(), request.getPort()
                    );
                    discoverySocket.send(response);
                }
            } catch (IOException ignored) {
            }
        }, "OnlineServer-Discovery");
        discoveryThread.setDaemon(true);
        discoveryThread.start();
    }

    private void startCleanupLoop() {
        Thread cleanupThread = new Thread(() -> {
            while (running) {
                synchronized (this) {
                    long now = System.currentTimeMillis();
                    Iterator<PlayerSlot> it = playersByToken.values().iterator();
                    while (it.hasNext()) {
                        PlayerSlot p = it.next();
                        if (!p.connected &&
                            now - p.disconnectTime > GRACE_PERIOD_MS &&
                            !"HOST".equals(p.token)) {

                            turnOrder.remove(p.token);

                            if (currentTurnIndex >= turnOrder.size()) {
                                currentTurnIndex = 0;
                            }

                            broadcast("PLAYER_REMOVED|" + p.name);
                            it.remove();
                            
                            if (gameStarted) expectedPlayersAtStart--;
                            checkScoreboard(); 
                        }
                    }
                }
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }, "OnlineServer-Cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    // =========================
    // Client Session
    // =========================

    private class ClientSession {
        private final Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private PlayerSlot slot;

        ClientSession(Socket socket) {
            this.socket = socket;
        }

        void start() {
            Thread thread = new Thread(this::run, "OnlineServer-ClientSession");
            thread.setDaemon(true);
            thread.start();
        }

        void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

                String first = reader.readLine();
                if (first == null) return;

                if (first.startsWith("HELLO|")) {
                    handleHello(first.substring(6));
                } else if (first.startsWith("RECONNECT|")) {
                    handleReconnect(first.substring(10));
                } else if (first.startsWith("JOIN|")) {
                    handleHello(first.substring(5)); // รองรับ client เก่า
                } else {
                    send("ERROR|Invalid handshake");
                    return;
                }

                String line;
                while (running && (line = reader.readLine()) != null) {
                    if (line.startsWith("SCORE|")) {
                        int score = Integer.parseInt(line.substring(6));
                        synchronized (OnlineServer.this) {
                            slot.score = score;
                            slot.scoreSubmitted = true;
                            checkScoreboard();
                        }
                    }
                    else if (line.equals("READY")) {
                        synchronized (OnlineServer.this) {
                            slot.ready = true;
                            broadcastReadyStatus();
                            checkAllReady();
                        }
                    }
                    else if (line.equals("UNREADY")) {
                        synchronized (OnlineServer.this) {
                            slot.ready = false;
                            broadcastReadyStatus();
                        }
                    }
                    else if (line.equals("START_REQUEST")) {
                        synchronized (OnlineServer.this) {
                            if (!slot.isHost) {
                                send("ERROR|Only host can start");
                                continue;
                            }
                            if (!areAllReady()) {
                                send("ERROR|Not everyone ready");
                                continue;
                            }
                            startGame();
                        }
                    }
                    else if (line.equals("QUIT")) {
                        break;
                    }
                }
            } catch (IOException ignored) {
            } finally {
                handleDisconnect();
                close();
                sessions.remove(this);
            }
        }

        void handleHello(String name) {
            synchronized (OnlineServer.this) {
                if (gameStarted) {
                    send("ERROR|Game has already started");
                    return;
                }

                if (playersByToken.size() >= maxPlayers) {
                    send("ERROR|Room full");
                    return;
                }

                PlayerSlot ps = new PlayerSlot();
                ps.playerId = UUID.randomUUID().toString();
                ps.token = UUID.randomUUID().toString();
                ps.name = uniqueName(sanitize(name));
                ps.connected = true;
                ps.session = this;
                ps.ready = false;
                turnOrder.add(ps.token);

                if (playersByToken.isEmpty()) {
                    ps.isHost = true;
                    send("ROLE|HOST");
                } else {
                    ps.isHost = false;
                    send("ROLE|PLAYER");
                }

                playersByToken.put(ps.token, ps);
                this.slot = ps;

                send("WELCOME|" + ps.playerId + "|" + ps.token + "|" +
                        roomName + "|" + maxPlayers + "|" + playersByToken.size());

                broadcastPlayerList();
                broadcastReadyStatus();
            }
        }

        void handleReconnect(String token) {
            synchronized (OnlineServer.this) {
                PlayerSlot ps = playersByToken.get(token);
                if (ps == null) {
                    send("ERROR|หมดเวลา Reconnect หรือ Token ไม่ถูกต้อง");
                    return;
                }
                ps.connected = true;
                ps.session = this;
                this.slot = ps;

                send("STATE_SYNC|" + buildStateSync());
                broadcast("PLAYER_RECONNECTED|" + ps.name);
            }
            broadcastCurrentTurn();
            broadcastReadyStatus();
            broadcastPlayerList(); 
        }

        void handleDisconnect() {
            synchronized (OnlineServer.this) {
                if (slot != null) {
                    slot.connected = false;
                    slot.disconnectTime = System.currentTimeMillis();
                    slot.session = null;

                    broadcast("PLAYER_DISCONNECTED|" + slot.name);

                    if (!turnOrder.isEmpty()) {
                        String currentToken = turnOrder.get(currentTurnIndex);
                        if (slot.token.equals(currentToken)) {
                            nextTurn();
                        }
                    }

                    if (!gameStarted && !slot.isHost) {
                        playersByToken.remove(slot.token);
                        turnOrder.remove(slot.token);
                        
                        if (currentTurnIndex >= turnOrder.size()) {
                            currentTurnIndex = 0;
                        }
                    }

                    broadcastPlayerList();
                    broadcastReadyStatus();
                    checkAllReady(); 
                    if (gameStarted) {
                        checkScoreboard();
                    }
                }
            }
        }

        void send(String msg) {
            if (writer != null) writer.println(msg);
        }

        void close() {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    // =========================
    // Scoreboard
    // =========================

    private void checkScoreboard() {
        if (!gameStarted || scoreboardSent) return;

        long activePlayers = playersByToken.values().stream()
            .filter(p -> p.connected)
            .count();

        long submitted = playersByToken.values().stream()
            .filter(p -> p.scoreSubmitted)
            .count();

        if (submitted >= expectedPlayersAtStart || (activePlayers > 0 && submitted >= activePlayers)) {
            scoreboardSent = true;
            String board = buildScoreboard();
            broadcast("SCOREBOARD|" + board.replace("\n", "\\n"));

            if (listener != null) {
                SwingUtilities.invokeLater(() ->
                        listener.onScoreboardReady(board));
            }
            
            gameStarted = false;
            scoreboardSent = false;
            for (PlayerSlot p : playersByToken.values()) {
                p.scoreSubmitted = false;
                p.ready = false; 
                p.score = 0;
            }
            broadcastReadyStatus();
        }
    }

    private String buildScoreboard() {
        List<PlayerSlot> ranking = new ArrayList<>(playersByToken.values());
        ranking.sort((a, b) -> Integer.compare(b.score, a.score));

        StringBuilder sb = new StringBuilder();
        sb.append("ผลคะแนนห้องออนไลน์\n");

        for (int i = 0; i < ranking.size(); i++) {
            sb.append(i + 1)
              .append(") ")
              .append(ranking.get(i).name)
              .append(" - ")
              .append(ranking.get(i).score)
              .append("\n");
        }
        return sb.toString().trim();
    }

    // =========================
    // Broadcast
    // =========================

    private void broadcast(String msg) {
        for (PlayerSlot p : playersByToken.values()) {
            if (p.connected && p.session != null) {
                p.session.send(msg);
            }
        }
    }

    private void broadcastPlayerList() {
        List<String> list = new ArrayList<>();
        for (PlayerSlot p : playersByToken.values()) {
            list.add(p.name + (p.connected ? "" : " (DC)"));
        }
        broadcast("PLAYER_LIST|" + String.join(",", list));
        notifyPlayerList();
    }

    private void notifyPlayerList() {
        if (listener != null) {
            List<String> snapshot = new ArrayList<>();
            for (PlayerSlot p : playersByToken.values()) {
                snapshot.add(p.name);
            }
            SwingUtilities.invokeLater(() ->
                    listener.onPlayerListChanged(snapshot));
        }
    }

    // =========================
    // Utils
    // =========================

    private String uniqueName(String base) {
        Set<String> names = new HashSet<>();
        for (PlayerSlot p : playersByToken.values()) names.add(p.name);

        if (!names.contains(base)) return base;
        int i = 2;
        while (names.contains(base + "_" + i)) i++;
        return base + "_" + i;
    }

    private static String sanitize(String text) {
        if (text == null || text.trim().isEmpty()) return "Player";
        return text.trim().replace("|", "_").replace(",", "_").replace(";", "_").replace(":", "_");
    }

    // =========================
    // State Sync (Reconnect)
    // =========================
    
    private String buildStateSync() {
        StringBuilder sb = new StringBuilder();

        sb.append("ROOM=").append(roomName).append(";");
        sb.append("GAME_STARTED=").append(gameStarted).append(";");
        sb.append("PLAYERS=");

        List<String> list = new ArrayList<>();
        for (PlayerSlot p : playersByToken.values()) {
            list.add(p.name + ":" +
             p.score + ":" +
             (p.connected ? "1" : "0") + ":" +
             (p.ready ? "1" : "0"));
        }

        sb.append(String.join(",", list));
        sb.append(";CURRENT_TURN=");

        if (!turnOrder.isEmpty()) {
            String token = turnOrder.get(currentTurnIndex);
            PlayerSlot p = playersByToken.get(token);
            if (p != null) {
                sb.append(p.name);
            }
        }
        return sb.toString();
    }

    public synchronized boolean areAllReady() {
        if (playersByToken.isEmpty()) return false;

        for (PlayerSlot p : playersByToken.values()) {
            if (!p.ready) return false;
        }
        return true;
    }

    private void checkAllReady() {
        if (areAllReady()) {
            broadcast("ALL_READY");
        }
    }

    private void broadcastReadyStatus() {
        List<String> list = new ArrayList<>();
        for (PlayerSlot p : playersByToken.values()) {
            list.add(p.name + ":" + p.ready);
        }
        broadcast("READY_STATUS|" + String.join(",", list));
    }
}