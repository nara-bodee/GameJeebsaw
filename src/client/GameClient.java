package client;

import shared.network.DataPacket;
import shared.network.PacketCodec;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * GameClient - Client-side network handler
 * Manages connection to game server and receives packets
 */
public class GameClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private Consumer<DataPacket> onPacket;

    public void setOnPacket(Consumer<DataPacket> onPacket) {
        this.onPacket = onPacket;
    }

    /**
     * Connect to game server
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        // Start receiving loop
        new Thread(this::receiveLoop).start();
        System.out.println("Connected to server at " + host + ":" + port);
    }

    /**
     * Receive loop - continuously reads packets from server
     */
    private void receiveLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                DataPacket p = PacketCodec.fromJson(line);
                if (onPacket != null) {
                    onPacket.accept(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Receive loop error: " + e.getMessage());
        }
    }

    /**
     * Send packet to server
     */
    public void send(DataPacket packet) {
        if (out == null) {
            System.err.println("Not connected to server");
            return;
        }
        out.println(PacketCodec.toJson(packet));
    }

    /**
     * Disconnect from server
     */
    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }
}
