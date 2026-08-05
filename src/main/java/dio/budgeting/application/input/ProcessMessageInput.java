package dio.budgeting.application.input;

public record ProcessMessageInput(byte[] audioBytes, String text, String userId) {}
