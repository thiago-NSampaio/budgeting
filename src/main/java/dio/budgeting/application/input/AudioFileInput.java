package dio.budgeting.application.input;

public record AudioFileInput(
    String interactionId,
    byte[] audioData
) {

}