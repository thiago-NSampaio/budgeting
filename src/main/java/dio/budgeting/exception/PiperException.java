package dio.budgeting.exception;

public class PiperException extends RuntimeException {

    public PiperException(String message) {
        super(message);
    }

    public PiperException(String message, Throwable cause) {
        super(message, cause);
    }
}