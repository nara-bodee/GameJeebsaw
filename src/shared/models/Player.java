package shared.models;

public class Player {
    public String playerId;     // P1, P2, P3
    public String name;
    public int love = 0;
    public int charm = 0;
    public int money = 0;
    public boolean connected = true;

    public Player() {}

    public Player(String playerId, String name) {
        this.playerId = playerId;
        this.name = name;
    }
}
