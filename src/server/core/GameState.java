package server.core;

import shared.models.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameState - maintains complete game state snapshot for STATE_SYNC
 * DEV BIRD responsibility: Track all game state for synchronization to reconnected players
 */
public class GameState {
    public int dayIndex = 1;                // 1..7 (7 days total)
    public int turnIndex = 0;               // 0..2 (3 turns per day)
    public String currentEventId = "D1_E1"; // Event identifier
    public int timerRemainingSec = 15;      // Timer for current turn

    public List<Player> players = new ArrayList<>();

    public GameState() {}

    /**
     * Create a snapshot of the entire game state for transmission
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("dayIndex", dayIndex);
        map.put("turnIndex", turnIndex);
        map.put("currentEventId", currentEventId);
        map.put("timerRemainingSec", timerRemainingSec);
        map.put("players", players);
        return map;
    }

    /**
     * Helper: get player by ID
     */
    public Player getPlayer(String playerId) {
        for (Player p : players) {
            if (p.playerId.equals(playerId)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Helper: check if game should end (day > 7)
     */
    public boolean isGameOver() {
        return dayIndex > 7;
    }

    /**
     * Helper: advance to next day
     */
    public void advanceDay() {
        dayIndex++;
        currentEventId = "D" + dayIndex + "_E1";
    }
}
