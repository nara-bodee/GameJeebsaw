package core;

/**
 * A utility class to validate game state data received from the server.
 * Ensures that the game state abides by the core rules of the game.
 */
public class GameStateValidator {

    private static final int MIN_DAY = 1;
    private static final int MAX_DAY = 7;
    private static final int MIN_TURN = 0;
    private static final int MAX_TURN = 2;

    /**
     * Validates the core components of the game state.
     *
     * @param day The current day in the game (should be 1-7).
     * @param turn The current turn within the day (should be 0-2).
     * @return true if the state is valid, false otherwise.
     */
    public static boolean isValid(int day, int turn) {
        boolean isDayValid = (day >= MIN_DAY && day <= MAX_DAY);
        if (!isDayValid) {
            System.err.println("Validation failed: Day is out of range (1-7). Received: " + day);
            return false;
        }

        boolean isTurnValid = (turn >= MIN_TURN && turn <= MAX_TURN);
        if (!isTurnValid) {
            System.err.println("Validation failed: Turn is out of range (0-2). Received: " + turn);
            return false;
        }

        return true;
    }
}