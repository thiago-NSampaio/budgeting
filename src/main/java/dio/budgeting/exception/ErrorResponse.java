package dio.budgeting.exception;

import java.time.LocalDateTime;

/**
 * DTO para resposta de erro padronizada.
 */
public record ErrorResponse(
        String error,
        String message,
        LocalDateTime timestamp
) {}
