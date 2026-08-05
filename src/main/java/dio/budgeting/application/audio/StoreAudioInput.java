package dio.budgeting.application.audio;

public record StoreAudioInput(
    String interactionId,
    byte[] audioData
) {
    public StoreAudioInput {
        if (interactionId == null || interactionId.isBlank()) {
            throw new IllegalArgumentException("interactionId is required");
        }
        if (audioData == null || audioData.length == 0) {
            throw new IllegalArgumentException("audioData is required");
        }
    }
}
