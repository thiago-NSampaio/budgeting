package dio.budgeting.service;

public interface AudioStorageService {
    String store(String interactionId, byte[] audio);
}
