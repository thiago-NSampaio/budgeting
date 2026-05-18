package dio.budgeting.exception;

/**
 * Exception personalizada para erros na transcrição de áudio.
 */
public class TranscriptionException extends RuntimeException {

    public TranscriptionException(String message) {
        super(message);
    }

    public TranscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
