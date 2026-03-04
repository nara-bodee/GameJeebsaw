package online;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LanDiscovery {

    public static final int DISCOVERY_PORT = 44555;
    private static final String DISCOVERY_REQUEST = "GJ_DISCOVER";

    public static List<OnlineRoomInfo> discoverRooms(int timeoutMs) {
        Map<String, OnlineRoomInfo> foundRooms = new LinkedHashMap<>();

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(timeoutMs);

            byte[] requestData = DISCOVERY_REQUEST.getBytes(StandardCharsets.UTF_8);
            DatagramPacket requestPacket = new DatagramPacket(
                requestData,
                requestData.length,
                InetAddress.getByName("255.255.255.255"),
                DISCOVERY_PORT
            );
            socket.send(requestPacket);

            long endTime = System.currentTimeMillis() + timeoutMs;
            byte[] buffer = new byte[1024];

            while (System.currentTimeMillis() < endTime) {
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(response);
                } catch (SocketTimeoutException e) {
                    break;
                }

                String message = new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
                if (!message.startsWith("GJ_ROOM|")) {
                    continue;
                }

                String[] parts = message.split("\\|", 6);
                if (parts.length < 6) {
                    continue;
                }

                String roomName = parts[1];
                int port = parseInt(parts[2], -1);
                int currentPlayers = parseInt(parts[3], 0);
                int maxPlayers = parseInt(parts[4], 3);
                String hostName = parts[5];

                if (port <= 0) {
                    continue;
                }

                String hostAddress = response.getAddress().getHostAddress();
                String key = hostAddress + ":" + port;
                foundRooms.put(key, new OnlineRoomInfo(roomName, hostAddress, port, currentPlayers, maxPlayers, hostName));
            }
        } catch (Exception ignored) {
        }

        return new ArrayList<>(foundRooms.values());
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
