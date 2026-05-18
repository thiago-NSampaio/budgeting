package dio.budgeting.service;

import dio.budgeting.exception.TranscriptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Serviço para transcrição de áudio usando Whisper local.
 * 
 * Implementação production-ready com:
 * - Consumo de stdout/stderr em threads separadas para evitar deadlock
 * - Suporte completo a Windows via `py -m whisper`
 * - Timeout configurável
 * - Logging detalhado
 * - Limpeza adequada de recursos
 * - Tratamento robusto de InterruptedException
 */
@Service
public class TranscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(TranscriptionService.class);

    private static final String MODEL = "base";
    private static final String LANGUAGE = "pt";
    private static final long PROCESS_TIMEOUT_SECONDS = 300; // 5 minutos
    private static final long TXT_FILE_WAIT_TIMEOUT_MS = 30_000; // 30 segundos máximo
    private static final long TXT_FILE_POLL_INTERVAL_MS = 100; // polling a cada 100ms
    private static final String TMP_FILE_PREFIX = "audio_";
    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.contains("win");

    /**
     * Construtor que injeta dependências.
     */
    public TranscriptionService() {
        logger.info("TranscriptionService inicializado para SO: {} ({})", OS_NAME, 
            IS_WINDOWS ? "Windows" : "Unix-like");
    }

    /**
     * Transcreve um arquivo de áudio usando Whisper.
     *
     * @param audioFile arquivo multipart contendo o áudio
     * @return texto transcrito
     * @throws TranscriptionException se houver erro na transcrição
     */
    public String transcribeAudio(MultipartFile audioFile) {
        validarArquivo(audioFile);

        Path audioFilePath = null;
        Path txtFilePath = null;
        try {
            logger.info("Iniciando transcrição de áudio. Nome: {}, Tamanho: {} bytes",
                    audioFile.getOriginalFilename(), audioFile.getSize());

            // Criar arquivo temporário
            audioFilePath = criarArquivoTemporario(audioFile);
            logger.debug("Arquivo temporário criado em: {}", audioFilePath);

            // Executar Whisper
            txtFilePath = executarWhisper(audioFilePath);
            logger.debug("Arquivo de transcrição gerado em: {}", txtFilePath);

            // Ler transcrição
            String transcription = lerArquivoTranscricao(txtFilePath);
            logger.info("Transcrição concluída com sucesso. Tamanho: {} caracteres", 
                transcription.length());

            return transcription;

        } catch (TranscriptionException e) {
            logger.error("Erro de transcrição: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado durante transcrição", e);
            throw new TranscriptionException("Erro ao processar áudio: " + e.getMessage(), e);
        } finally {
            // Limpar arquivos temporários
            limparArquivo(audioFilePath);
            limparArquivo(txtFilePath);
        }
    }

    /**
     * Valida o arquivo de áudio.
     */
    private void validarArquivo(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new TranscriptionException("Arquivo de áudio não foi enviado ou está vazio");
        }

        String filename = audioFile.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new TranscriptionException("Nome do arquivo inválido");
        }

        logger.debug("Arquivo validado: {}", filename);
    }

    /**
     * Cria um arquivo temporário a partir do MultipartFile.
     */
    private Path criarArquivoTemporario(MultipartFile audioFile) throws IOException {
        String originalName = audioFile.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf('.'));

        Path tempFile = Files.createTempFile(TMP_FILE_PREFIX, extension);
        logger.debug("Transferindo arquivo para: {}", tempFile);
        audioFile.transferTo(tempFile.toFile());
        
        return tempFile;
    }

    /**
     * Executa o comando Whisper via ProcessBuilder.
     * 
     * Evita deadlock ao consumir stdout/stderr em threads separadas.
     * Aguarda corretamente a geração do arquivo .txt de transcrição.
     * 
     * @param audioFilePath caminho do arquivo de áudio
     * @return caminho do arquivo .txt gerado
     * @throws TranscriptionException se houver erro na execução
     */
    private Path executarWhisper(Path audioFilePath) throws TranscriptionException {
        List<String> command = construirComandoWhisper(audioFilePath);

        logger.info("Executando Whisper com comando: {}", String.join(" ", command));
        logger.debug("Arquivo de entrada: {}", audioFilePath.toAbsolutePath());

        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            
            // Redirecionar stderr para stdout para capturar todos os logs do Whisper
            pb.redirectErrorStream(true);
            
            // Iniciar processo
            process = pb.start();
            logger.debug("Processo Whisper iniciado com PID: {}", process.pid());

            // Consumir stdout/stderr em thread separada para evitar deadlock
            ProcessOutputConsumer outputConsumer = new ProcessOutputConsumer(process);
            Thread outputThread = new Thread(outputConsumer);
            outputThread.setDaemon(false);
            outputThread.start();
            logger.debug("Thread de consumo de output iniciada");

            // Aguardar término do processo com timeout
            boolean completed = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!completed) {
                logger.error("Timeout ao executar Whisper após {} segundos", PROCESS_TIMEOUT_SECONDS);
                process.destroyForcibly();
                throw new TranscriptionException(
                    "Timeout ao executar Whisper (>" + PROCESS_TIMEOUT_SECONDS + "s)");
            }

            // Aguardar thread de output (com timeout)
            outputThread.join(5_000); // 5 segundos de timeout

            int exitCode = process.exitValue();
            String output = outputConsumer.getOutput();

            logger.debug("Processo Whisper finalizado com código de saída: {}", exitCode);
            logger.debug("Saída do Whisper:\n{}", output);

            if (exitCode != 0) {
                logger.error("Whisper retornou código de erro: {}\nSaída:\n{}", exitCode, output);
                throw new TranscriptionException(
                    "Whisper falhou com código de erro " + exitCode);
            }

            // Aguardar geração do arquivo .txt
            Path txtFilePath = aguardarArquivoTranscricao(audioFilePath);
            logger.debug("Arquivo de transcrição gerado com sucesso: {}", txtFilePath);

            return txtFilePath;

        } catch (InterruptedException e) {
            logger.error("Transcrição foi interrompida", e);
            
            // Limpar processo se ainda estiver rodando
            if (process != null && process.isAlive()) {
                logger.warn("Destruindo processo Whisper após interrupção");
                process.destroyForcibly();
            }
            
            // Restaurar interrupted status
            Thread.currentThread().interrupt();
            
            throw new TranscriptionException(
                "Transcrição foi interrompida: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Erro ao iniciar processo Whisper", e);
            throw new TranscriptionException(
                "Erro ao iniciar Whisper: " + e.getMessage(), e);
        } catch (TranscriptionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao executar Whisper", e);
            throw new TranscriptionException(
                "Erro ao executar Whisper: " + e.getMessage(), e);
        }
    }

    /**
     * Aguarda a geração do arquivo .txt de transcrição do Whisper.
     * 
     * @param audioFilePath caminho do arquivo de áudio
     * @return caminho do arquivo .txt gerado
     * @throws TranscriptionException se o arquivo não for encontrado no tempo limite
     */
    private Path aguardarArquivoTranscricao(Path audioFilePath) throws TranscriptionException {
        String audioFileName = audioFilePath.getFileName().toString();
        String txtFileName = audioFileName.substring(0, audioFileName.lastIndexOf('.')) + ".txt";
        Path txtFilePath = audioFilePath.getParent().resolve(txtFileName);

        logger.debug("Aguardando geração do arquivo de transcrição: {}", txtFilePath);

        long startTime = System.currentTimeMillis();
        long maxWaitTime = TXT_FILE_WAIT_TIMEOUT_MS;

        while (true) {
            if (Files.exists(txtFilePath)) {
                long waitTime = System.currentTimeMillis() - startTime;
                logger.debug("Arquivo de transcrição encontrado após {} ms", waitTime);
                return txtFilePath;
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > maxWaitTime) {
                logger.error("Arquivo de transcrição não foi gerado dentro de {} ms", maxWaitTime);
                throw new TranscriptionException(
                    "Arquivo de transcrição não foi gerado. Caminho esperado: " + txtFilePath);
            }

            try {
                Thread.sleep(TXT_FILE_POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                logger.error("Espera pela geração do arquivo foi interrompida", e);
                Thread.currentThread().interrupt();
                throw new TranscriptionException(
                    "Espera pelo arquivo de transcrição foi interrompida", e);
            }
        }
    }

    /**
     * Constrói o comando Whisper compatível com Windows e Unix.
     * 
     * Usa `py -m whisper` para compatibilidade com Windows.
     */
    private List<String> construirComandoWhisper(Path audioFilePath) {
        List<String> command = new ArrayList<>();

        // Usar `py` para Windows, `python3` para Unix-like
        if (IS_WINDOWS) {
            command.add("py");
        } else {
            command.add("python3");
        }

        command.add("-m");
        command.add("whisper");

        // Arquivo de entrada (com aspas para caminhos com espaços)
        command.add(audioFilePath.toAbsolutePath().toString());

        // Modelo
        command.add("--model");
        command.add(MODEL);

        // Idioma
        command.add("--language");
        command.add(LANGUAGE);

        // Formato de saída
        command.add("--output_format");
        command.add("txt");

        // Diretório de saída (explícito para garantir que o .txt seja gerado lá)
        command.add("--output_dir");
        command.add(audioFilePath.getParent().toString());

        // Desabilitar fp16 (importante para compatibilidade)
        command.add("--fp16");
        command.add("False");

        return command;
    }

    /**
     * Lê o conteúdo do arquivo .txt de transcrição.
     */
    private String lerArquivoTranscricao(Path txtFilePath) throws IOException {
        logger.debug("Lendo arquivo de transcrição: {}", txtFilePath);
        String transcription = Files.readString(txtFilePath, StandardCharsets.UTF_8).trim();
        logger.debug("Transcrição lida. Tamanho: {} caracteres", transcription.length());
        return transcription;
    }

    /**
     * Remove um arquivo de forma segura.
     */
    private void limparArquivo(Path filePath) {
        if (filePath != null) {
            try {
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    logger.debug("Arquivo deletado: {}", filePath);
                }
            } catch (IOException e) {
                logger.warn("Falha ao deletar arquivo: {} - {}", filePath, e.getMessage());
            }
        }
    }

    /**
     * Classe interna para consumir stdout/stderr do processo em thread separada.
     * 
     * Isso evita deadlock quando há muito output do processo.
     */
    private static class ProcessOutputConsumer implements Runnable {
        private final Process process;
        private final StringBuilder output = new StringBuilder();
        private static final Logger outputLogger = LoggerFactory.getLogger(
            ProcessOutputConsumer.class);

        ProcessOutputConsumer(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    outputLogger.debug("[Whisper] {}", line);
                }
            } catch (IOException e) {
                outputLogger.error("Erro ao ler output do processo", e);
            }
        }

        String getOutput() {
            return output.toString();
        }
    }
}

