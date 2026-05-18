# Refatoração do Serviço de Transcrição - Whisper Local

## 📋 Resumo das Mudanças

O serviço `TranscriptionService` foi **completamente refatorado** para ser **production-ready** com suporte robusto a ProcessBuilder no Windows.

### ✅ Principais Melhorias

#### 1. **Evita Deadlock em ProcessBuilder**
- **Problema Original**: Consumir stdout/stderr de forma síncrona pode causar deadlock se o buffer ficar cheio
- **Solução**: Nova classe `ProcessOutputConsumer` que consome output em thread separada
- **Benefício**: O processo nunca fica bloqueado esperando por leitura de stdout/stderr

```java
// Antes (❌ Deadlock potencial):
process.waitFor();
BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
output = reader.lines().reduce("", ...);

// Depois (✅ Seguro):
ProcessOutputConsumer outputConsumer = new ProcessOutputConsumer(process);
Thread outputThread = new Thread(outputConsumer);
outputThread.start();
process.waitFor(TIMEOUT, TimeUnit.SECONDS);
outputThread.join(5_000);
```

#### 2. **Timeout Robusto e Determinístico**
- ⏱️ Timeout principal: **300 segundos** (5 minutos) para execução do Whisper
- ⏱️ Timeout auxiliar: **30 segundos** para aguardar geração do arquivo .txt
- ⏱️ Polling: **100ms** entre verificações de existência do arquivo
- Destroi processo forçadamente se timeout

#### 3. **Compatibilidade Completa com Windows**
```java
if (IS_WINDOWS) {
    command.add("py");      // ✅ Windows Python launcher
} else {
    command.add("python3"); // ✅ Unix-like
}
```

#### 4. **Tratamento Correto de InterruptedException**
```java
catch (InterruptedException e) {
    if (process != null && process.isAlive()) {
        process.destroyForcibly();
    }
    Thread.currentThread().interrupt(); // ✅ Restaura flag
    throw new TranscriptionException(...);
}
```

#### 5. **Aguarda Corretamente o Arquivo .txt**
```java
// Antes (❌ Frágil):
for (int i = 0; i < 10; i++) {
    if (Files.exists(txtFilePath)) break;
    Thread.sleep(200);
}

// Depois (✅ Robusto):
long maxWaitTime = TXT_FILE_WAIT_TIMEOUT_MS;
while (System.currentTimeMillis() - startTime < maxWaitTime) {
    if (Files.exists(txtFilePath)) return txtFilePath;
    Thread.sleep(TXT_FILE_POLL_INTERVAL_MS);
}
```

#### 6. **Logging Production-Ready**
- Logs em diferentes níveis (DEBUG, INFO, ERROR, WARN)
- Informações contextuais: PID do processo, códigos de saída, tempos
- Output do Whisper capturado em logs

```
INFO: Executando Whisper com comando: py -m whisper ...
DEBUG: Processo Whisper iniciado com PID: 12345
DEBUG: Thread de consumo de output iniciada
DEBUG: Arquivo de transcrição encontrado após 245 ms
INFO: Transcrição concluída com sucesso. Tamanho: 1250 caracteres
DEBUG: Arquivo deletado: C:\Temp\audio_xyz123.ogg
```

#### 7. **Limpeza Segura de Recursos**
```java
finally {
    limparArquivo(audioFilePath);  // Arquivo de entrada
    limparArquivo(txtFilePath);    // Arquivo de saída
}
```

---

## 🔧 Constantes Configuráveis

```java
private static final long PROCESS_TIMEOUT_SECONDS = 300;      // 5 minutos
private static final long TXT_FILE_WAIT_TIMEOUT_MS = 30_000;   // 30 segundos
private static final long TXT_FILE_POLL_INTERVAL_MS = 100;     // 100 ms
private static final String MODEL = "base";
private static final String LANGUAGE = "pt";
```

**Para ajustar timeout em ambiente com CPU lenta:**
```java
private static final long PROCESS_TIMEOUT_SECONDS = 600;  // 10 minutos para ambientes lento
```

---

## 📝 Comando Whisper Construído

```bash
py -m whisper "C:\Temp\audio_xyz.ogg" \
    --model base \
    --language pt \
    --output_format txt \
    --output_dir "C:\Temp" \
    --fp16 False
```

**Por que `--fp16 False`?**
- Desabilita precisão reduzida (float16)
- Importante para compatibilidade em CPUs antigas
- Evita erros em GPUs com pouca VRAM

---

## 🧪 Como Testar

### 1. **Via cURL (Linux/Mac/WSL)**
```bash
curl -X POST \
  -F "audio=@/path/to/audio.ogg" \
  http://localhost:8080/transcription
```

### 2. **Via PowerShell (Windows)**
```powershell
$audioFile = Get-Item "C:\Users\tiago\Downloads\audio1.ogg"
$form = @{
    audio = $audioFile
}
Invoke-WebRequest -Uri "http://localhost:8080/transcription" `
    -Method POST `
    -Form $form
```

### 3. **Via Postman**
1. POST: `http://localhost:8080/transcription`
2. Body → form-data
3. Key: `audio`, Type: `File`
4. Value: selecionar arquivo de áudio

### 4. **Resposta Esperada**
```json
{
  "transcription": "Olá, esta é uma transcrição de teste do Whisper."
}
```

---

## 🐛 Diagnóstico de Problemas

### Erro: "Arquivo de transcrição não foi gerado"
```
Causa: Whisper não completou a geração do .txt
Solução: 
1. Aumentar TXT_FILE_WAIT_TIMEOUT_MS para 60_000
2. Verificar saída do Whisper nos logs (buscar [Whisper])
3. Validar permissões de escrita no diretório temporário
```

### Erro: "Timeout ao executar Whisper"
```
Causa: Processo levou mais de 300 segundos
Solução:
1. Aumentar PROCESS_TIMEOUT_SECONDS para 600
2. Verificar se CPU está sobrecarregada
3. Testar Whisper manualmente: py -m whisper "audio.ogg"
```

### Processo travado em "Executando Whisper"
```
Causa: Deadlock (antes da refatoração) ou stderr cheio
Solução: ✅ Corrigido! A refatoração resolve isso consumindo output em thread separada
```

---

## 📊 Fluxo de Execução

```
┌─ validarArquivo()
│  └─ Valida MultipartFile
│
├─ criarArquivoTemporario()
│  └─ Cria arquivo temp em %TEMP%
│
├─ executarWhisper()
│  ├─ construirComandoWhisper()
│  ├─ ProcessBuilder.start()
│  ├─ ProcessOutputConsumer (thread)
│  ├─ process.waitFor(timeout)
│  ├─ aguardarArquivoTranscricao()
│  └─ Limpar se erro
│
├─ lerArquivoTranscricao()
│  └─ Ler .txt gerado
│
└─ finally: limparArquivo()
   ├─ Deletar arquivo temp
   └─ Deletar .txt
```

---

## 🎯 Benefícios Production-Ready

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Deadlock** | ❌ Possível | ✅ Impossível |
| **Timeout** | ❌ Frágil (não aguarda .txt) | ✅ Robusto (2 níveis) |
| **Windows** | ⚠️ Parcial (hardcoded `py`) | ✅ Detecção automática |
| **InterruptedException** | ❌ Não tratado | ✅ Correto com restauração |
| **Logging** | ⚠️ Básico | ✅ Detalhado e contextual |
| **Limpeza** | ⚠️ Pode falhar silenciosamente | ✅ Com retry e logs |
| **Java 21** | ⚠️ Não aproveitado | ✅ Best practices |

---

## 🚀 Próximos Passos Sugeridos

1. **Adicionar métricas de performance**
   ```java
   long startTime = System.currentTimeMillis();
   // ... processamento ...
   logger.info("Tempo total de transcrição: {} ms", 
       System.currentTimeMillis() - startTime);
   ```

2. **Implementar cache de transcrições**
   - Hash do arquivo de áudio como chave
   - Cache em memória ou Redis

3. **Suporte a múltiplos modelos**
   ```java
   public String transcribeAudio(MultipartFile audio, String model) {
       this.MODEL = model; // ou usar como parâmetro
   }
   ```

4. **Limite de tamanho de arquivo**
   ```java
   if (audioFile.getSize() > 50 * 1024 * 1024) {  // 50 MB
       throw new TranscriptionException("Arquivo muito grande");
   }
   ```

5. **Integração com banco de dados**
   - Salvar histórico de transcrições
   - Rastrear tempo de processamento

---

## 📚 Referências

- [OpenAI Whisper GitHub](https://github.com/openai/whisper)
- [Java ProcessBuilder Documentation](https://docs.oracle.com/javase/21/docs/api/java.base/java/lang/ProcessBuilder.html)
- [Spring Boot MultipartFile](https://spring.io/guides/gs/uploading-files/)

---

**Status**: ✅ Production-Ready  
**Última atualização**: 17 de maio de 2026  
**Compilação**: ✅ Sucesso
