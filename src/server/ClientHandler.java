package server;

import shared.network.DataPacket;
import shared.network.PacketCodec;
import shared.network.PacketType;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

/**
 * ClientHandler - handles individual client connection
 * Manages HELLO/RECONNECT handshake and forwards packets to GameEngine
 */
public class ClientHandler extends Thread {
    private final Socket socket;
    private final GameServer server;

    private BufferedReader in;
    private PrintWriter out;

    private String playerId;
    private String token;
    private boolean registered = false;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            // First packet must be HELLO or RECONNECT
            String firstLine = in.readLine();
            if (firstLine == null) return;

            DataPacket first = PacketCodec.fromJson(firstLine);
            if (PacketType.HELLO.equals(first.type)) {
                handleHello(first);
            } else if (PacketType.RECONNECT.equals(first.type)) {
                handleReconnect(first);
            } else {
                send(DataPacket.of(PacketType.REJECT).with("reason", "First packet must be HELLO or RECONNECT"));
                return;
            }

            // Keep reading game packets
            String line;
            while ((line = in.readLine()) != null) {
                DataPacket packet = PacketCodec.fromJson(line);
                packet.playerId = this.playerId;
                packet.token = this.token;
                packet.roomId = server.getRoom().roomId;

                // Forward to game engine
                server.getRoom().engine.onPacket(this, packet, server);
            }

        } catch (Exception e) {
            // Connection error
        } finally {
            onDisconnect();
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Handle HELLO packet - new player joining
     */
    private void handleHello(DataPacket packet) {
        String name = (String) packet.payload.getOrDefault("name", "Player");

        // Assign player ID based on current player count
        int idx = server.getRoom().handlers.size() + 1;
        this.playerId = "P" + idx;

        // Generate and store token
        this.token = "tkn_" + UUID.randomUUID().toString().substring(0, 8);
        server.getRegistry().putToken(token, playerId);

        // Register in room
        server.getRoom().handlers.add(this);
        registered = true;

        // Send confirmation to client
        DataPacket ok = DataPacket.of("HELLO_OK")
                .with("playerId", playerId)
                .with("token", token)
                .with("roomId", server.getRoom().roomId);
        send(ok);

        System.out.println("Player " + playerId + " (" + name + ") joined room");

        // If 3 players reached, start game
        if (server.getRoom().handlers.size() == 3) {
            System.out.println("All 3 players connected, starting game!");
            ServerBroadcaster.broadcast(server.getRoom(), DataPacket.of(PacketType.START_GAME));
            server.getRoom().engine.onStartGame(server);
        }
    }

    /**
     * Handle RECONNECT packet - player reconnecting
     */
    private void handleReconnect(DataPacket packet) {
        String t = (String) packet.payload.get("token");
        String resolved = server.getRegistry().resolvePlayerId(t);

        if (resolved == null) {
            send(DataPacket.of(PacketType.REJECT).with("reason", "token_invalid"));
            System.out.println("Reconnect failed: invalid token");
            return;
        }

        this.token = t;
        this.playerId = resolved;

        // Re-register handler in room
        server.getRoom().handlers.add(this);
        registered = true;

        System.out.println("Player " + playerId + " reconnected");

        // Tell engine to sync state
        server.getRoom().engine.onReconnect(this, server);
    }

    /**
     * Called when client disconnects
     */
    private void onDisconnect() {
        if (!registered) return;

        // Remove from handler list
        server.getRoom().handlers.remove(this);

        System.out.println("Player " + playerId + " disconnected");

        // Notify engine
        server.getRoom().engine.onDisconnect(playerId, server);
    }

    /**
     * Send packet to this client
     */
    public void send(DataPacket packet) {
        if (out == null) return;
        out.println(PacketCodec.toJson(packet));
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getToken() {
        return token;
    }
}
