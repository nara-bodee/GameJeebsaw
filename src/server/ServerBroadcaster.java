package server;

import shared.network.DataPacket;

/**
 * ServerBroadcaster - Helper for sending packets to all players in a room
 */
public class ServerBroadcaster {
    public static void broadcast(RoomSession room, DataPacket packet) {
        synchronized (room.handlers) {
            for (ClientHandler h : room.handlers) {
                h.send(packet);
            }
        }
    }

    public static void broadcastExcept(RoomSession room, DataPacket packet, ClientHandler exclude) {
        synchronized (room.handlers) {
            for (ClientHandler h : room.handlers) {
                if (h != exclude) {
                    h.send(packet);
                }
            }
        }
    }
}
