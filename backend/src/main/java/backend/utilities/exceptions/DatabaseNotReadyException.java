package backend.utilities.exceptions;

public class DatabaseNotReadyException extends RuntimeException {
    public DatabaseNotReadyException(String message) {
        super(message);
    }
}
