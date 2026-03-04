package shared.network;

public final class PacketType {
    private PacketType() {}

    // Handshake / Reconnect
    public static final String HELLO = "HELLO";                 // {name}
    public static final String RECONNECT = "RECONNECT";         // {token}
    public static final String STATE_SYNC = "STATE_SYNC";       // {fullState}
    public static final String REJECT = "REJECT";               // {reason}

    // Game flow
    public static final String START_GAME = "START_GAME";
    public static final String TURN_START = "TURN_START";       // {currentTurnPlayerId, timerSec}
    public static final String CHOICE_SUBMIT = "CHOICE_SUBMIT"; // {eventId, choiceId}
    public static final String STATE_UPDATE = "STATE_UPDATE";   // {fullState}
    public static final String TIME_OUT = "TIME_OUT";           // {playerId}
    public static final String GAME_OVER = "GAME_OVER";         // {winnerPlayerId}

    // Presence
    public static final String PLAYER_DISCONNECTED = "PLAYER_DISCONNECTED"; // {playerId, graceSec}
    public static final String PLAYER_RECONNECTED = "PLAYER_RECONNECTED";   // {playerId}
}
