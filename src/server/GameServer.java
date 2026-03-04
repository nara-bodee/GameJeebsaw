package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * GameServer - Main server that accepts client connections
 * Manages room and session registry
 */
public class GameServer {
    private final int port;
    private ServerSocket serverSocket;

    private final RoomSession room = new RoomSession("ROOM1");
    private final SessionRegistry registry = new SessionRegistry();

    public GameServer(int port) {
        this.port = port;
    }

    public RoomSession getRoom() {
        return room;
    }

    public SessionRegistry getRegistry() {
        return registry;
    }

    /**
     * Start the server and accept client connections
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("=" + "=".repeat(40));
        System.out.println("GameServer started on port: " + port);
        System.out.println("=" + "=".repeat(40));

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connection from " + socket.getInetAddress());
            ClientHandler handler = new ClientHandler(socket, this);
            handler.start();
        }
    }

    /**
     * Entry point for server
     */
    public static void main(String[] args) {
        int port = 7777;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        try {
            new GameServer(port).start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
