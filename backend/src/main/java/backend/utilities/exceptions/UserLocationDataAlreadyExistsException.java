package backend.utilities.exceptions;

public class UserLocationDataAlreadyExistsException extends RuntimeException {
    public UserLocationDataAlreadyExistsException(String message) {
        super(message);
    }
}
