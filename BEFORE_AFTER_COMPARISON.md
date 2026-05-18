# Comparação Antes vs Depois - Mudanças Críticas

## 📊 Análise Lado a Lado

### ❌ ANTES: Método executarWhisper com Deadlock Potencial

```java
private String executarWhisper(Path audioFilePath)
        throws IOException, InterruptedException {

    List<String> command = construirComandoWhisper(audioFilePath);

    logger.info("Executando Whisper: {}", String.join(" ", command));

    ProcessBuilder processBuilder = new ProcessBuilder(command);

    processBuilder.redirectErrorStream(true);  // ⚠️ Redireciona stderr para stdout

    Process process = processBuilder.start();

    // ❌ PROBLEMA: Aguarda processo mas não lê output simultaneamente
    // Se stdout/stderr buffer ficar cheio, processo fica bloqueado
    // e este código TRAVA aqui esperando waitFor() terminar
    boolean completed = process.waitFor(
            PROCESS_TIMEOUT_SECONDS,
            TimeUnit.SECONDS);

    if (!completed) {
        process.destroyForcibly();
        throw new TranscriptionException("Timeout ao executar Whisper");
    }

    String output;

    // ❌ PROBLEMA: Tenta ler output agora, mas pode estar vazio ou truncado
    // porque stdout pode ter transbordado durante execução
    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(
                    process.getInputStream(),
                    StandardCharsets.UTF_8))) {
        output = reader.lines()
                .reduce("", (a, b) -> a + b + "\n");
    }

    logger.info("Whisper output:\n{}", output);

    int exitCode = process.exitValue();

    if (exitCode != 0) {
        throw new TranscriptionException(
                "Erro ao executar Whisper:\n" + output);
    }

    // ❌ PROBLEMA: Frágil - espera no máximo 2 segundos
    // Se Whisper escrever em disco lentamente, arquivo não é encontrado
    return lerArquivoTranscricao(audioFilePath);  // Isso lê e deleta .txt
}
```

**Problemas Identificados:**
1. 🔴 **DEADLOCK**: Aguarda `waitFor()` sem consumir output
2. 🔴 **TIMEOUT FRÁGIL**: `lerArquivoTranscricao()` aguarda apenas ~2 segundos
3. 🔴 **ARQUIVO VAZIO**: Output pode não ser capturado corretamente
4. 🟡 **SEM LOGGING**: Difícil debugar problemas

---

### ✅ DEPOIS: Método executarWhisper Refatorado

```java
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
        
        // ✅ SOLUÇÃO: Consumir output em thread SEPARADA
        // Isso evita deadlock - o processo nunca fica bloqueado escrevendo para stdout
        ProcessOutputConsumer outputConsumer = new ProcessOutputConsumer(process);
        Thread outputThread = new Thread(outputConsumer);
        outputThread.setDaemon(false);
        outputThread.start();
        logger.debug("Thread de consumo de output iniciada");

        // ✅ Aguardar processo com timeout adequado
        boolean completed = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        if (!completed) {
            logger.error("Timeout ao executar Whisper após {} segundos", PROCESS_TIMEOUT_SECONDS);
            process.destroyForcibly();
            throw new TranscriptionException(
                "Timeout ao executar Whisper (>" + PROCESS_TIMEOUT_SECONDS + "s)");
        }

        // ✅ Aguardar thread de output (com timeout)
        outputThread.join(5_000);  // 5 segundos de timeout

        int exitCode = process.exitValue();
        String output = outputConsumer.getOutput();  // ✅ Output foi capturado continuamente

        logger.debug("Processo Whisper finalizado com código de saída: {}", exitCode);
        logger.debug("Saída do Whisper:\n{}", output);

        if (exitCode != 0) {
            logger.error("Whisper retornou código de erro: {}\nSaída:\n{}", exitCode, output);
            throw new TranscriptionException(
                "Whisper falhou com código de erro " + exitCode);
        }

        // ✅ Aguardar geração do arquivo .txt com timeout robusto (até 30 segundos)
        Path txtFilePath = aguardarArquivoTranscricao(audioFilePath);
        logger.debug("Arquivo de transcrição gerado com sucesso: {}", txtFilePath);

        return txtFilePath;  // ✅ Retorna caminho do arquivo, não a transcrição

    } catch (InterruptedException e) {
        logger.error("Transcrição foi interrompida", e);
        
        // ✅ Limpar processo se ainda estiver rodando
        if (process != null && process.isAlive()) {
            logger.warn("Destruindo processo Whisper após interrupção");
            process.destroyForcibly();
        }
        
        // ✅ IMPORTANTE: Restaurar interrupted status
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
```

**Melhorias Implementadas:**
1. ✅ **THREAD SEPARADA**: `ProcessOutputConsumer` consome output continuamente
2. ✅ **SEM DEADLOCK**: Processo nunca fica bloqueado esperando leitura
3. ✅ **TIMEOUT ROBUSTO**: `aguardarArquivoTranscricao()` aguarda até 30 segundos
4. ✅ **LOGGING COMPLETO**: PID, exit code, tempos, output
5. ✅ **TRATAMENTO DE INTERRUPÇÃO**: Restaura thread flag corretamente
6. ✅ **SEPARAÇÃO DE RESPONSABILIDADES**: Retorna Path em vez de String

---

## 🔄 Nova Classe: ProcessOutputConsumer

```java
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
        // ✅ Executa em thread separada
        // ✅ Lê continuamente de stdout
        // ✅ Armazena em StringBuilder
        // ✅ Nunca bloqueia o processo
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
```

**Por que funciona:**
1. ✅ Executa em thread separada, não bloqueia main
2. ✅ Consome stdout continuamente
3. ✅ Armazena output para análise posterior
4. ✅ Loga cada linha do Whisper para debug
5. ✅ Nunca pode causar deadlock

---

## ⏱️ Novo Método: aguardarArquivoTranscricao

```java
// ❌ ANTES: Frágil (máximo 2 segundos)
private String lerArquivoTranscricao(Path audioFilePath) {
    Path txtFilePath = audioFilePath.getParent().resolve(
        audioFileName.substring(0, audioFileName.lastIndexOf('.')) + ".txt"
    );
    
    for (int i = 0; i < 10; i++) {  // 10 iterações
        if (Files.exists(txtFilePath)) break;
        Thread.sleep(200);  // 200ms * 10 = 2 segundos máximo
    }
    
    // ❌ Se não encontrar, quebra com FileNotFoundException
    String transcription = Files.readString(txtFilePath, StandardCharsets.UTF_8);
    Files.delete(txtFilePath);
    return transcription;
}

// ✅ DEPOIS: Robusto (até 30 segundos)
private Path aguardarArquivoTranscricao(Path audioFilePath) throws TranscriptionException {
    String audioFileName = audioFilePath.getFileName().toString();
    String txtFileName = audioFileName.substring(0, audioFileName.lastIndexOf('.')) + ".txt";
    Path txtFilePath = audioFilePath.getParent().resolve(txtFileName);

    logger.debug("Aguardando geração do arquivo de transcrição: {}", txtFilePath);

    long startTime = System.currentTimeMillis();
    long maxWaitTime = TXT_FILE_WAIT_TIMEOUT_MS;  // 30 segundos

    while (true) {
        if (Files.exists(txtFilePath)) {
            long waitTime = System.currentTimeMillis() - startTime;
            logger.debug("Arquivo de transcrição encontrado após {} ms", waitTime);
            return txtFilePath;  // ✅ Retorna caminho
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime > maxWaitTime) {
            logger.error("Arquivo de transcrição não foi gerado dentro de {} ms", maxWaitTime);
            throw new TranscriptionException(
                "Arquivo de transcrição não foi gerado. Caminho esperado: " + txtFilePath);
        }

        try {
            Thread.sleep(TXT_FILE_POLL_INTERVAL_MS);  // 100ms
        } catch (InterruptedException e) {
            logger.error("Espera pela geração do arquivo foi interrompida", e);
            Thread.currentThread().interrupt();  // ✅ Restaura flag
            throw new TranscriptionException(
                "Espera pelo arquivo de transcrição foi interrompida", e);
        }
    }
}
```

**Melhorias:**
1. ✅ Aguarda até 30 segundos (300 iterações de 100ms)
2. ✅ Retorna Path, deixa leitura para caller
3. ✅ Trata InterruptedException corretamente
4. ✅ Logging de tempo real de espera
5. ✅ Falha clara se timeout

---

## 🖥️ Detecção de SO

```java
// ❌ ANTES: Hardcoded, não funciona em Windows
private List<String> construirComandoWhisper(Path audioFilePath) {
    List<String> command = new ArrayList<>();
    command.add("py");  // Windows OK, mas...
    // ... em Unix, pode não existir
}

// ✅ DEPOIS: Detecta automaticamente
private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();
private static final boolean IS_WINDOWS = OS_NAME.contains("win");

private List<String> construirComandoWhisper(Path audioFilePath) {
    List<String> command = new ArrayList<>();

    if (IS_WINDOWS) {
        command.add("py");        // Windows: use py launcher
    } else {
        command.add("python3");   // Unix/Linux/macOS: use python3
    }
    
    command.add("-m");
    command.add("whisper");
    // ... resto do comando
}
```

---

## 🛡️ Tratamento de InterruptedException

```java
// ❌ ANTES: Não trata corretamente
try {
    process.waitFor();
} catch (InterruptedException e) {
    throw new TranscriptionException(...);
    // ❌ Flag de interrupção perdida!
}

// ✅ DEPOIS: Trata conforme Java best practices
catch (InterruptedException e) {
    logger.error("Transcrição foi interrompida", e);
    
    if (process != null && process.isAlive()) {
        logger.warn("Destruindo processo Whisper após interrupção");
        process.destroyForcibly();  // ✅ Limpar recurso
    }
    
    Thread.currentThread().interrupt();  // ✅ Restaurar flag
    
    throw new TranscriptionException(
        "Transcrição foi interrompida: " + e.getMessage(), e);
}
```

---

## 📊 Tabela de Comparação

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Consumo de Output** | Síncrono (bloqueante) | Assíncrono (thread) |
| **Deadlock** | Possível | Impossível |
| **Timeout .txt** | 2 segundos (frágil) | 30 segundos (robusto) |
| **Polling .txt** | 10 iterações fixas | Até 300 iterações com timing real |
| **Windows** | Funciona (py) | Detecta (py ou python3) |
| **InterruptedException** | Não tratado | Tratado corretamente |
| **PID do Processo** | Não capturado | Capturado para debug |
| **Logging** | Mínimo | Completo em múltiplos níveis |
| **Tratamento de Erro** | Genérico | Específico e contextual |
| **Linhas de Código** | ~100 | ~180 (mais robusto) |

---

## 🎯 Resultado

**Antes:** Código frágil, propenso a deadlock, timeout frágil, difícil de debugar

**Depois:** Código robusto, production-ready, timeout confiável, logging completo

---

**Data**: 17 de maio de 2026  
**Status**: ✅ Refatoração Completa
