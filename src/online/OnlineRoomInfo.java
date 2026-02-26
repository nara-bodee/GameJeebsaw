package online;

public class OnlineRoomInfo {
    private final String roomName;
    private final String hostAddress;
    private final int port;
    private final int currentPlayers;
    private final int maxPlayers;
    private final String hostName;

    public OnlineRoomInfo(String roomName, String hostAddress, int port, int currentPlayers, int maxPlayers, String hostName) {
        this.roomName = roomName;
        this.hostAddress = hostAddress;
        this.port = port;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
        this.hostName = hostName;
    }

    public String getRoomName() { return roomName; }
    public String getHostAddress() { return hostAddress; }
    public int getPort() { return port; }
    public int getCurrentPlayers() { return currentPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public String getHostName() { return hostName; }

    @Override
    public String toString() {
        return roomName + " | Host: " + hostName + " | " + currentPlayers + "/" + maxPlayers + " | " + hostAddress + ":" + port;
    }
}
