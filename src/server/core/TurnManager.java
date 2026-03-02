package server.core;

import java.util.List;

public class TurnManager {
    private int turnIndex = 0;  // 0, 1, 2 within a day

    public int getTurnIndex() {
        return turnIndex;
    }

    public void setTurnIndex(int idx) {
        this.turnIndex = idx;
    }

    public String currentPlayerId(List<String> orderedPlayerIds) {
        if (orderedPlayerIds == null || orderedPlayerIds.isEmpty()) return null;
        return orderedPlayerIds.get(turnIndex % orderedPlayerIds.size());
    }

    public void nextTurn(List<String> orderedPlayerIds) {
        if (orderedPlayerIds == null || orderedPlayerIds.isEmpty()) return;
        turnIndex = (turnIndex + 1) % orderedPlayerIds.size();
    }

    public boolean isStartOfNewDay(List<String> orderedPlayerIds) {
        return turnIndex == 0;
    }
}
