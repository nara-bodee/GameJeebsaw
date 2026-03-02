package online;

/**
 * An example GameClient demonstrating token persistence and state validation.
 */
public class GameClient {

    // A dummy interface to represent the UI interactions for this example.
    interface GameUI {
        void showLobby();
        void updateState(int day, int turn);
    }

    private final GameUI gameUI;

    public GameClient(GameUI gameUI) {
        this.gameUI = gameUI;
    }

    /**
     * The main entry point for the client's startup logic.
     */
    public void start() {
        // Step 1: On start, check if a token exists.
        String token = ClientTokenManager.loadToken();

        // Step 2: If it exists, attempt reconnect. Otherwise, go to lobby.
        if (token != null && !token.isEmpty()) {
            System.out.println("Found existing token. Attempting Reconnect...");
            // This would be a network call to the server, e.g., sendMessage("RECONNECT|" + token);
            reconnectWithToken(token);
        } else {
            System.out.println("No token found. Go to Lobby.");
            // This would show the main menu or lobby screen.
            gameUI.showLobby();
        }
    }

    /**
     * Simulates a network call to reconnect and handles simulated server responses.
     */
    private void reconnectWithToken(String token) {
        // --- SIMULATING SERVER RESPONSES ---
        // Scenario 1: Server says token is invalid.
        // handleServerMessage("ERROR|INVALID_TOKEN");

        // Scenario 2: Server accepts token and sends game state.
        handleServerMessage("STATE|3|1|...other data...");
    }

    /**
     * Handles messages received from the server.
     */
    public void handleServerMessage(String message) {
        String[] parts = message.split("\\|");
        String command = parts[0];

        switch (command) {
            case "STATE":
                // Step 4 (Sync): Validate game state before updating UI.
                try {
                    int day = Integer.parseInt(parts[1]);
                    int turn = Integer.parseInt(parts[2]);

                    if (GameStateValidator.isValid(day, turn)) {
                        System.out.println("Game state is valid. Updating UI.");
                        gameUI.updateState(day, turn);
                    } else {
                        System.err.println("Received invalid game state from server. Ignoring.");
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error parsing game state from server: " + message);
                }
                break;

            case "ERROR":
                // Step 3 (Fallback Flow): Invalid token response from server.
                if (parts.length > 1 && "INVALID_TOKEN".equals(parts[1])) {
                    System.out.println("Server rejected token. Deleting local token.");
                    ClientTokenManager.deleteToken();

                    System.out.println("Returning to Lobby.");
                    gameUI.showLobby();
                } else {
                    System.err.println("Received error from server: " + message);
                }
                break;

            default:
                System.out.println("Received from server: " + message);
                break;
        }
    }

    // Example of how this might be run
    public static void main(String[] args) {
        // Create a dummy UI implementation for the example
        GameUI dummyUI = new GameUI() {
            @Override public void showLobby() { System.out.println("[UI] Showing Lobby Screen."); }
            @Override public void updateState(int day, int turn) { System.out.println("[UI] Updated to Day: " + day + ", Turn: " + turn); }
        };

        GameClient client = new GameClient(dummyUI);
        client.start();
    }
}