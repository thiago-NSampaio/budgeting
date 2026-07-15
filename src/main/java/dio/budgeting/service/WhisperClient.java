package dio.budgeting.service;

import java.io.InputStream;

public interface WhisperClient {
    String transcribe(InputStream audio) throws Exception;
}
