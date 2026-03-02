package server.core;

import server.ClientHandler;
import server.GameServer;
import server.ServerBroadcaster;
import server.data.StoryLoader;
import shared.models.Choice;
import shared.models.Event;
import shared.models.Player;
import shared.network.DataPacket;
import shared.network.PacketType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GameEngine - Main game logic orchestrator
 * DEV BIRD responsibility: Handle game flow, state management, and game logic
 */
public class GameEngine {
    private final TurnManager turnManager = new TurnManager();
    private final GameState state = new GameState();
    private final StoryLoader storyLoader = new StoryLoader();

    // Player order (P1, P2, P3)
    private final List<String> playerOrder = new ArrayList<>();

    // Tracking disconnected players for grace period
    private final Map<String, Long> disconnectedAt = new ConcurrentHashMap<>();

    // Cache loaded events
    private Map<String, Event> eventCache = new HashMap<>();

    public GameEngine() {
        // Load all story events
        try {
            List<Event> events = storyLoader.loadEvents();
            for (Event e : events) {
                eventCache.put(e.eventId, e);
            }
        } catch (Exception e) {
            System.err.println("Failed to load story events: " + e.getMessage());
        }
    }

    // ==================== Game Initialization ====================

    /**
     * Called when 3 players have connected
     */
    public void onStartGame(GameServer server) {
        state.players.clear();
        playerOrder.clear();

        // Initialize all 3 players
        for (int i = 1; i <= 3; i++) {
            String pid = "P" + i;
            playerOrder.add(pid);
            Player p = new Player(pid, "Player " + pid);
            state.players.add(p);
        }

        // Set starting event
        state.dayIndex = 1;
        state.turnIndex = 0;
        state.currentEventId = "D1_E1";

        broadcastTurnStart(server);
        broadcastState(server);
    }

    // ==================== Packet Handling ====================

    /**
     * Main packet handler
     */
    public void onPacket(ClientHandler from, DataPacket packet, GameServer server) {
        switch (packet.type) {
            case PacketType.CHOICE_SUBMIT:
                handleChoice(from, packet, server);
                break;
            default:
                // Ignore unknown packets
                break;
        }
    }

    // ==================== Choice Handling with Story Effects ====================

    /**
     * Handle choice submission and apply story effects
     * DEV BIRD: Integrates with StoryLoader to apply choice effects
     */
    private void handleChoice(ClientHandler from, DataPacket packet, GameServer server) {
        String current = turnManager.currentPlayerId(playerOrder);
        if (!from.getPlayerId().equals(current)) {
            // Not this player's turn - ignore
            return;
        }

        // Get choice details
        String eventId = (String) packet.payload.get("eventId");
        String choiceId = (String) packet.payload.get("choiceId");

        // Find the event and choice
        Event event = eventCache.get(eventId);
        if (event == null) {
            System.err.println("Event not found: " + eventId);
            return;
        }

        Choice choice = findChoice(event, choiceId);
        if (choice == null) {
            System.err.println("Choice not found: " + choiceId);
            return;
        }

        // Apply choice effects to current player
        Player currentPlayer = state.getPlayer(from.getPlayerId());
        if (currentPlayer != null && choice.effect != null) {
            currentPlayer.love += choice.effect.love;
            currentPlayer.charm += choice.effect.charm;
            currentPlayer.money += choice.effect.money;
        }

        // Advance game
        turnManager.nextTurn(playerOrder);
        state.turnIndex = turnManager.getTurnIndex();

        // Check if day should advance (after each player's turn = 3 turns total)
        if (state.turnIndex == 0) {
            state.dayIndex++;
            if (state.dayIndex > 7) {
                endGame(server);
                return;
            }
            state.currentEventId = "D" + state.dayIndex + "_E1";
        }

        broadcastTurnStart(server);
        broadcastState(server);
    }

    private Choice findChoice(Event event, String choiceId) {
        if (event.choices == null) return null;
        for (Choice c : event.choices) {
            if (c.choiceId != null && c.choiceId.equals(choiceId)) {
                return c;
            }
        }
        return null;
    }

    // ==================== Disconnection & Reconnection ====================

    /**
     * Called when client disconnects
     * Sets DISCONNECTED status and starts grace period
     */
    public void onDisconnect(String playerId, GameServer server) {
        Player p = state.getPlayer(playerId);
        if (p != null) {
            p.connected = false;
        }

        disconnectedAt.put(playerId, System.currentTimeMillis());

        // Broadcast disconnect notice
        ServerBroadcaster.broadcast(server.getRoom(),
                DataPacket.of(PacketType.PLAYER_DISCONNECTED)
                        .with("playerId", playerId)
                        .with("graceSec", 60));

        // Start 60-second grace timer
        new Thread(() -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException ignored) {
            }

            // After grace period: if still disconnected, mark as timed out
            Player px = state.getPlayer(playerId);
            if (px != null && !px.connected) {
                // Implementation: skip their turns when they come up
                // No AI replacement - just skip (simple approach)
                System.out.println("Grace period expired for " + playerId);
            }
        }).start();
    }

    /**
     * Called when client reconnects with valid token
     * Sends STATE_SYNC and notifies other players
     */
    public void onReconnect(ClientHandler handler, GameServer server) {
        String playerId = handler.getPlayerId();
        Player p = state.getPlayer(playerId);
        if (p != null) {
            p.connected = true;
        }

        disconnectedAt.remove(playerId);

        // Send full state to reconnected client
        handler.send(DataPacket.of(PacketType.STATE_SYNC)
                .with("fullState", state.toMap()));

        // Notify other players of reconnection
        ServerBroadcaster.broadcast(server.getRoom(),
                DataPacket.of(PacketType.PLAYER_RECONNECTED)
                        .with("playerId", playerId));

        // Broadcast updated state to all
        broadcastState(server);
    }

    // ==================== Broadcasting ====================

    /**
     * Notify all clients about whose turn it is
     */
    private void broadcastTurnStart(GameServer server) {
        String current = turnManager.currentPlayerId(playerOrder);

        ServerBroadcaster.broadcast(server.getRoom(),
                DataPacket.of(PacketType.TURN_START)
                        .with("currentTurnPlayerId", current)
                        .with("timerSec", state.timerRemainingSec));
    }

    /**
     * Broadcast updated game state to all clients
     */
    private void broadcastState(GameServer server) {
        ServerBroadcaster.broadcast(server.getRoom(),
                DataPacket.of(PacketType.STATE_UPDATE)
                        .with("fullState", state.toMap()));
    }

    /**
     * End game and announce winner
     */
    private void endGame(GameServer server) {
        String winner = calculateWinner();
        ServerBroadcaster.broadcast(server.getRoom(),
                DataPacket.of(PacketType.GAME_OVER)
                        .with("winnerPlayerId", winner));
    }

    /**
     * Calculate winner based on love score (highest love wins)
     */
    private String calculateWinner() {
        return state.players.stream()
                .max(Comparator.comparingInt(p -> p.love))
                .map(p -> p.playerId)
                .orElse("P1");
    }

    // ==================== Getters ====================

    public GameState getState() {
        return state;
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    public Event getEvent(String eventId) {
        return eventCache.get(eventId);
    }
}
