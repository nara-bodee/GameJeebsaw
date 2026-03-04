package online;

/**
 * A utility class to validate game state data received from the server.
 * Ensures that the state abides by the core game rules.
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
     * @return {@code true} if the state is valid, {@code false} otherwise.
     */
    public static boolean isValid(int day, int turn) {
        boolean isDayValid = (day >= MIN_DAY && day <= MAX_DAY);
        boolean isTurnValid = (turn >= MIN_TURN && turn <= MAX_TURN);

        if (!isDayValid) System.err.println("Validation failed: Day " + day + " is out of range (1-7).");
        if (!isTurnValid) System.err.println("Validation failed: Turn " + turn + " is out of range (0-2).");

        return isDayValid && isTurnValid;
    }
}