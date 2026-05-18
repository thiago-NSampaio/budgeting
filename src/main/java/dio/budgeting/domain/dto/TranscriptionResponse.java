package dio.budgeting.domain.dto;

/**
 * DTO para resposta de transcrição de áudio.
 * Utiliza record para imutabilidade e simplicidade.
 */
public record TranscriptionResponse(
        String transcription
) {}
