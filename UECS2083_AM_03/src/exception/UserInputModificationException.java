package exception;
/**
 * Thrown when a user requests to modify their input and restart the data selection process.
 * This is a signal for graceful return to the Data Selection Menu, not a critical error.
 */
public class UserInputModificationException extends RuntimeException {
    public UserInputModificationException(String message) {
        super(message);
    }
}
