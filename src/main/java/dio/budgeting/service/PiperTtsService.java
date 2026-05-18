package dio.budgeting.service;

import dio.budgeting.exception.PiperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PiperTtsService {

    private static final Logger logger = LoggerFactory.getLogger(PiperTtsService.class);

    private static final String PIPER_EXECUTABLE = "piper";

private static final String MODEL_PATH =
        "C:\\Users\\tiago\\piper\\voices\\pt_BR-faber-medium.onnx";

    private static final long TIMEOUT_SECONDS = 120;

    public byte[] generateSpeech(String text) {
        validateText(text);

        Path outputFile = null;

        try {

            outputFile = Files.createTempFile(
                    "speech_",
                    ".wav"
            );

            List<String> command = buildCommand(outputFile);

            logger.info("Executando Piper TTS...");
            logger.info("Comando: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder processOutput = new StringBuilder();

            Thread outputThread = createOutputConsumer(
                    process,
                    processOutput
            );

            outputThread.start();
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            process.getOutputStream(),
                            StandardCharsets.UTF_8
                    )
            )) {

                writer.write(text);
                writer.flush();
            }

            boolean completed = process.waitFor(
                    TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );

            if (!completed) {
                process.destroyForcibly();

                throw new PiperException(
                        "Timeout ao executar Piper"
                );
            }

            outputThread.join();

            int exitCode = process.exitValue();

            logger.info("Piper finalizado com código {}", exitCode);

            if (exitCode != 0) {

                throw new PiperException(
                        "Erro ao executar Piper:\n" + processOutput
                );
            }

            waitForFile(outputFile);

            byte[] audio = Files.readAllBytes(outputFile);

            logger.info(
                    "Áudio gerado com sucesso. Tamanho: {} bytes",
                    audio.length
            );

            return audio;
} catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new PiperException(
                    "Thread interrompida",
                    e
            );

        } catch (IOException e) {

            throw new PiperException(
                    "Erro ao gerar áudio",
                    e
            );

        } finally {

            deleteTempFile(outputFile);
        }
    }

    private List<String> buildCommand(Path outputFile) {

        List<String> command = new ArrayList<>();

        command.add(PIPER_EXECUTABLE);

        command.add("--model");
        command.add(MODEL_PATH);

        command.add("--output_file");
        command.add(outputFile.toAbsolutePath().toString());

        return command;
    }

    private Thread createOutputConsumer(
            Process process,
            StringBuilder output
    ) {
                return new Thread(() -> {

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            process.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            )) {

                String line;

                while ((line = reader.readLine()) != null) {

                    logger.info("PIPER: {}", line);

                    output
                            .append(line)
                            .append(System.lineSeparator());
                }

            } catch (IOException e) {

                logger.error(
                        "Erro lendo output do Piper",
                        e
                );
            }
        });
    }

    private void waitForFile(Path outputFile)
            throws InterruptedException {

        for (int i = 0; i < 20; i++) {

            if (Files.exists(outputFile)) {
                return;
            }

            Thread.sleep(200);
        }

        throw new PiperException(
                "Arquivo WAV não foi gerado"
        );
    }

    private void validateText(String text) {

        if (text == null || text.isBlank()) {

            throw new PiperException(
                    "Texto não pode ser vazio"
            );
        }
    }

    private void deleteTempFile(Path file) {

        if (file == null) {
            return;
        }

        try {

            Files.deleteIfExists(file);

            logger.debug("Arquivo temporário removido: {}", file);

        } catch (IOException e) {

            logger.warn(
                    "Erro removendo arquivo temporário",
                    e
            );
        }
    }
}