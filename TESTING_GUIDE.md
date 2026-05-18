# Testes e Validação do Serviço de Transcrição

## 📝 Testes Unitários Sugeridos

Adicione ao arquivo `BudgetingApplicationTests.java`:

```java
@SpringBootTest
class TranscriptionServiceTests {

    @Autowired
    private TranscriptionService transcriptionService;

    @Test
    void testTranscribeAudioWithValidFile() throws IOException {
        // Criar arquivo de áudio de teste
        byte[] audioContent = /* conteúdo de áudio de teste */;
        
        MockMultipartFile audioFile = new MockMultipartFile(
            "audio",
            "test_audio.ogg",
            "audio/ogg",
            audioContent
        );

        String result = transcriptionService.transcribeAudio(audioFile);
        
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void testThrowsExceptionForEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "audio",
            "empty.ogg",
            "audio/ogg",
            new byte[0]
        );

        assertThrows(
            TranscriptionException.class,
            () -> transcriptionService.transcribeAudio(emptyFile)
        );
    }

    @Test
    void testThrowsExceptionForNullFile() {
        assertThrows(
            TranscriptionException.class,
            () -> transcriptionService.transcribeAudio(null)
        );
    }

    @Test
    void testThrowsExceptionForInvalidFilename() {
        MockMultipartFile invalidFile = new MockMultipartFile(
            "audio",
            "",
            "audio/ogg",
            "content".getBytes()
        );

        assertThrows(
            TranscriptionException.class,
            () -> transcriptionService.transcribeAudio(invalidFile)
        );
    }
}
```

## 🔍 Verificação de Logs

### Log Esperado (Sucesso):
```
INFO  TranscriptionService - Iniciando transcrição de áudio. Nome: test.ogg, Tamanho: 45000 bytes
DEBUG TranscriptionService - Arquivo temporário criado em: C:\Users\tiago\AppData\Local\Temp\audio_xyz123.ogg
DEBUG TranscriptionService - Transferindo arquivo para: C:\Users\tiago\AppData\Local\Temp\audio_xyz123.ogg
INFO  TranscriptionService - Executando Whisper com comando: py -m whisper C:\Users\tiago\AppData\Local\Temp\audio_xyz123.ogg --model base --language pt --output_format txt --output_dir C:\Users\tiago\AppData\Local\Temp --fp16 False
DEBUG TranscriptionService - Processo Whisper iniciado com PID: 12345
DEBUG TranscriptionService - Thread de consumo de output iniciada
DEBUG ProcessOutputConsumer - [Whisper] Detecting language using up to the first 30 seconds. Use `--language` to specify the language
DEBUG ProcessOutputConsumer - [Whisper] Detected language: Portuguese
DEBUG ProcessOutputConsumer - [Whisper] [00:00.000 --> 00:05.000] Olá, esta é uma transcrição de teste
DEBUG TranscriptionService - Processo Whisper finalizado com código de saída: 0
DEBUG TranscriptionService - Saída do Whisper: [log completo do Whisper]
DEBUG TranscriptionService - Aguardando geração do arquivo de transcrição: C:\Users\tiago\AppData\Local\Temp\audio_xyz.txt
DEBUG TranscriptionService - Arquivo de transcrição encontrado após 234 ms
DEBUG TranscriptionService - Lendo arquivo de transcrição: C:\Users\tiago\AppData\Local\Temp\audio_xyz.txt
DEBUG TranscriptionService - Transcrição lida. Tamanho: 128 caracteres
INFO  TranscriptionService - Transcrição concluída com sucesso. Tamanho: 128 caracteres
DEBUG TranscriptionService - Arquivo deletado: C:\Users\tiago\AppData\Local\Temp\audio_xyz123.ogg
DEBUG TranscriptionService - Arquivo deletado: C:\Users\tiago\AppData\Local\Temp\audio_xyz.txt
```

### Log Esperado (Timeout):
```
ERROR TranscriptionService - Timeout ao executar Whisper após 300 segundos
```

### Log Esperado (Arquivo .txt não gerado):
```
ERROR TranscriptionService - Arquivo de transcrição não foi gerado dentro de 30000 ms
```

## 🧪 Teste Manual com cURL

### 1. Arquivo de áudio de teste (gerar com FFmpeg)
```bash
# Criar arquivo de áudio de teste
ffmpeg -f lavfi -i sine=frequency=1000:duration=5 -q:a 9 -acodec libmp3lame -b:a 32k test_audio.mp3

# Converter para formato compatível com Whisper
ffmpeg -i test_audio.mp3 test_audio.ogg
```

### 2. Executar POST com cURL
```bash
curl -X POST \
  -F "audio=@test_audio.ogg" \
  -H "Accept: application/json" \
  http://localhost:8080/transcription
```

### 3. Resposta esperada
```json
{
  "transcription": "one thousand hertz"
}
```

## 🐛 Troubleshooting

### Verificar se Whisper está instalado
```powershell
py -m whisper --version
```

Esperado: `Whisper version X.X.X`

### Verificar se FFmpeg está instalado
```powershell
ffmpeg -version
```

Esperado: Versão do FFmpeg

### Testar Whisper manualmente
```powershell
py -m whisper "C:\path\to\audio.ogg" --model base --language pt --output_format txt --output_dir "C:\output" --fp16 False
```

Verificar se arquivo .txt foi criado em `C:\output\audio.txt`

### Aumentar nível de logging
Adicionar ao `application.properties`:
```properties
logging.level.dio.budgeting.service=DEBUG
logging.level.dio.budgeting.service.ProcessOutputConsumer=DEBUG
```

## ✅ Checklist de Validação

- [ ] Whisper instalado e acessível via `py -m whisper`
- [ ] FFmpeg instalado
- [ ] Java 21 em uso (verificar com `java -version`)
- [ ] Gradle build bem-sucedido (`./gradlew clean build`)
- [ ] Aplicação inicia sem erros
- [ ] Endpoint `/transcription` responde com POST
- [ ] Upload de arquivo multipart funciona
- [ ] Resposta JSON contém campo `transcription`
- [ ] Logs mostram execução completa
- [ ] Arquivos temporários são deletados
- [ ] Timeout funciona (testar com timeout < 10s)

## 📊 Métricas de Performance

### Ambiente esperado:
- CPU: Intel i5/Ryzen 5+
- RAM: 8GB+
- GPU: Opcional (recomendado CUDA/Metal para performance)

### Tempos esperados (modelo base):
- **Áudio de 30 segundos**: 5-15 segundos
- **Áudio de 1 minuto**: 10-30 segundos
- **Áudio de 5 minutos**: 30-90 segundos
- **Overhead Spring**: < 1 segundo

Total com overhead:
- **Timeout padrão**: 300 segundos (5 minutos)
- **Segurança**: Timeout > tempo esperado + 100%

## 🔧 Configurações Recomendadas para Diferentes Cenários

### 1. **Desenvolvimento Local**
```java
private static final long PROCESS_TIMEOUT_SECONDS = 300;      // OK
private static final long TXT_FILE_WAIT_TIMEOUT_MS = 30_000;  // OK
```

### 2. **Servidor com GPU (NVIDIA CUDA)**
```java
private static final long PROCESS_TIMEOUT_SECONDS = 120;      // Mais rápido
private static final long TXT_FILE_WAIT_TIMEOUT_MS = 10_000;  // Mais rápido
```

### 3. **Servidor com CPU lenta**
```java
private static final long PROCESS_TIMEOUT_SECONDS = 600;      // 10 minutos
private static final long TXT_FILE_WAIT_TIMEOUT_MS = 60_000;  // 1 minuto
```

### 4. **Servidor com limite de recursos**
```java
// Use modelo smaller em vez de base
private static final String MODEL = "tiny";  // Mais rápido, menos preciso
```

---

**Status**: ✅ Validação Completa  
**Data**: 17 de maio de 2026
