# Exemplos Práticos - Como Usar o Novo Serviço

## 🚀 Teste Rápido (5 minutos)

### 1. Criar arquivo de áudio de teste
```powershell
# Com FFmpeg (recomendado)
ffmpeg -f lavfi -i sine=frequency=1000:duration=5 -q:a 9 -acodec libmp3lame -b:a 32k test.mp3
ffmpeg -i test.mp3 test.ogg

# Ou baixar um arquivo
# https://www.sample-videos.com/download-sample-ogg.php
```

### 2. Iniciar aplicação
```bash
./gradlew bootRun
```

Log esperado:
```
INFO  TranscriptionService - TranscriptionService inicializado para SO: ...
INFO  BudgetingApplication - Started BudgetingApplication in X seconds
```

### 3. Enviar requisição
```powershell
# PowerShell
$file = Get-Item "test.ogg"
$form = @{ audio = $file }
$response = Invoke-WebRequest -Uri "http://localhost:8080/transcription" `
    -Method POST -Form $form -SkipHttpErrorCheck
    
# Exibir resultado
$response.Content | ConvertFrom-Json
```

**Resposta esperada:**
```json
{
  "transcription": "one thousand hertz"
}
```

---

## 📋 Testes Detalhados

### Teste 1: Arquivo válido
```powershell
# ✅ ESPERADO: Sucesso
$file = Get-Item "audio_valido.ogg"
$form = @{ audio = $file }
$response = Invoke-WebRequest -Uri "http://localhost:8080/transcription" `
    -Method POST -Form $form

# HTTP 200
# {"transcription": "..."}
```

### Teste 2: Arquivo vazio
```powershell
# ❌ ESPERADO: Erro
New-Item "empty.ogg" -Value ""
$file = Get-Item "empty.ogg"
$form = @{ audio = $file }

$response = Invoke-WebRequest -Uri "http://localhost:8080/transcription" `
    -Method POST -Form $form -SkipHttpErrorCheck

# HTTP 400/500
# {"message": "Arquivo de áudio não foi enviado ou está vazio"}
```

### Teste 3: Sem arquivo
```powershell
# ❌ ESPERADO: Erro (Spring valida)
Invoke-WebRequest -Uri "http://localhost:8080/transcription" `
    -Method POST `
    -Form @{} `
    -SkipHttpErrorCheck

# HTTP 400
# Multi-part request parameter 'audio' not found
```

### Teste 4: Arquivo muito grande
```powershell
# ❌ ESPERADO: Erro (arquivo > 100MB)
# O arquivo será rejeitado pela validação do Spring
# HTTP 413 Payload Too Large
```

### Teste 5: Timeout (simular)
```powershell
# Para simular timeout, editar TranscriptionService.java:
# private static final long PROCESS_TIMEOUT_SECONDS = 1;  # 1 segundo

# ❌ ESPERADO: Erro
$file = Get-Item "audio_longo.ogg"  # Áudio com 5+ minutos
$form = @{ audio = $file }
$response = Invoke-WebRequest -Uri "http://localhost:8080/transcription" `
    -Method POST -Form $form -SkipHttpErrorCheck

# HTTP 500
# {"message": "Timeout ao executar Whisper (>1s)"}
```

---

## 📊 Logs de Debug

### Ativar DEBUG logging
Em `application.properties`:
```properties
logging.level.dio.budgeting.service=DEBUG
logging.level.dio.budgeting.service.ProcessOutputConsumer=DEBUG
```

### Logs esperados para sucesso
```
15:30:45.123 [http-nio-8080-exec-1] DEBUG TranscriptionService - Arquivo validado: audio.ogg
15:30:45.145 [http-nio-8080-exec-1] DEBUG TranscriptionService - Arquivo temporário criado em: C:\temp\audio_xyz.ogg
15:30:45.156 [http-nio-8080-exec-1] INFO  TranscriptionService - Executando Whisper com comando: py -m whisper ...
15:30:45.234 [http-nio-8080-exec-1] DEBUG TranscriptionService - Processo Whisper iniciado com PID: 12345
15:30:45.235 [http-nio-8080-exec-1] DEBUG TranscriptionService - Thread de consumo de output iniciada
15:30:45.240 [output-thread-1] DEBUG ProcessOutputConsumer - [Whisper] Detecting language...
15:30:45.450 [output-thread-1] DEBUG ProcessOutputConsumer - [Whisper] Detected language: Portuguese
15:30:45.890 [output-thread-1] DEBUG ProcessOutputConsumer - [Whisper] [00:00.000 --> 00:05.000] one thousand hertz
15:30:48.123 [http-nio-8080-exec-1] DEBUG TranscriptionService - Processo Whisper finalizado com código de saída: 0
15:30:48.134 [http-nio-8080-exec-1] DEBUG TranscriptionService - Aguardando geração do arquivo de transcrição: C:\temp\audio_xyz.txt
15:30:48.245 [http-nio-8080-exec-1] DEBUG TranscriptionService - Arquivo de transcrição encontrado após 111 ms
15:30:48.267 [http-nio-8080-exec-1] DEBUG TranscriptionService - Transcrição lida. Tamanho: 22 caracteres
15:30:48.268 [http-nio-8080-exec-1] INFO  TranscriptionService - Transcrição concluída com sucesso. Tamanho: 22 caracteres
15:30:48.289 [http-nio-8080-exec-1] DEBUG TranscriptionService - Arquivo deletado: C:\temp\audio_xyz.ogg
15:30:48.290 [http-nio-8080-exec-1] DEBUG TranscriptionService - Arquivo deletado: C:\temp\audio_xyz.txt
```

### Logs esperados para erro
```
15:35:10.123 [http-nio-8080-exec-1] DEBUG TranscriptionService - Arquivo validado: audio.ogg
15:35:10.145 [http-nio-8080-exec-1] DEBUG TranscriptionService - Arquivo temporário criado em: C:\temp\audio_xyz.ogg
15:35:10.156 [http-nio-8080-exec-1] INFO  TranscriptionService - Executando Whisper com comando: py -m whisper ...
15:35:10.234 [http-nio-8080-exec-1] DEBUG TranscriptionService - Processo Whisper iniciado com PID: 12346
15:35:10.235 [http-nio-8080-exec-1] DEBUG TranscriptionService - Thread de consumo de output iniciada
15:35:10.240 [output-thread-1] DEBUG ProcessOutputConsumer - [Whisper] Error: Audio file is corrupted
15:35:12.123 [http-nio-8080-exec-1] DEBUG TranscriptionService - Processo Whisper finalizado com código de saída: 1
15:35:12.134 [http-nio-8080-exec-1] ERROR TranscriptionService - Whisper retornou código de erro: 1
15:35:12.145 [http-nio-8080-exec-1] DEBUG TranscriptionService - Arquivo deletado: C:\temp\audio_xyz.ogg
```

---

## 🔧 Configurações por Cenário

### Cenário 1: Desenvolvimento Local
```properties
# application.properties
logging.level.dio.budgeting.service=DEBUG
server.port=8080
spring.servlet.multipart.max-file-size=100MB
```

Editar em `TranscriptionService.java`:
```java
private static final long PROCESS_TIMEOUT_SECONDS = 300;      // 5 min
private static final long TXT_FILE_WAIT_TIMEOUT_MS = 30_000;   // 30 seg
```

### Cenário 2: Servidor com GPU (NVIDIA CUDA)
```properties
# application.properties
logging.level.dio.budgeting.service=INFO
server.tomcat.threads.max=200
spring.servlet.multipart.max-file-size=500MB
```

Editar em `TranscriptionService.java`:
```java
private static final long PROCESS_TIMEOUT_SECONDS = 120;      // 2 min (mais rápido)
private static final long TXT_FILE_WAIT_TIMEOUT_MS = 10_000;   // 10 seg
private static final String MODEL = "base";  // ou "small" se quiser mais rápido
```

### Cenário 3: Servidor CPU Lento
```properties
# application.properties
logging.level.dio.budgeting.service=INFO
server.tomcat.threads.max=50
spring.servlet.multipart.max-file-size=50MB
```

Editar em `TranscriptionService.java`:
```java
private static final long PROCESS_TIMEOUT_SECONDS = 600;      // 10 min
private static final long TXT_FILE_WAIT_TIMEOUT_MS = 60_000;   // 1 min
private static final String MODEL = "tiny";  // Mais rápido, menos preciso
```

---

## 📈 Métricas para Monitorar

### 1. Tempo de Processamento
```
Métrica: Quanto tempo levou do POST até a resposta?
Esperado: 5-60 segundos dependendo do tamanho
```

### 2. Taxa de Sucesso
```
Métrica: Quantas requisições completaram com sucesso?
Esperado: > 95%
```

### 3. Taxa de Timeout
```
Métrica: Quantas requisições deram timeout?
Esperado: < 5% (ou 0%)
```

### 4. Uso de Memória
```
Métrica: Quanto RAM o Java está usando?
Esperado: < 500MB para processo único
```

### 5. Tamanho Médio de Arquivo
```
Métrica: Qual é o tamanho médio dos áudios?
Esperado: 1-50MB
```

---

## 🐛 Troubleshooting em Tempo Real

### Problema: "Process is still alive" (após timeout)
```
Diagnóstico:
- Executar: ps aux | grep whisper (Linux)
- Executar: tasklist | findstr whisper (Windows)

Solução:
- Verificar se destroyForcibly() está funcionando
- Aumentar timeout
- Reiniciar Java
```

### Problema: "OutOfMemory"
```
Diagnóstico:
- Verificar: jps -l (listar Java processes)
- Executar: jstat -gc <pid> 1000 (monitor GC)

Solução:
- Aumentar heap: java -Xmx2048m (2GB)
- Reduzir tamanho máximo de arquivo
- Usar modelo "tiny" em vez de "base"
```

### Problema: Arquivo .txt não é encontrado
```
Diagnóstico:
- Verificar se %TEMP% tem espaço em disco
- Verificar permissões: icacls %TEMP%
- Verificar se Whisper criou arquivo: dir %TEMP%\audio_*

Solução:
- Limpar %TEMP%: del /Q %TEMP%\audio_*
- Aumentar TXT_FILE_WAIT_TIMEOUT_MS
- Verificar logs do Whisper
```

---

## ✅ Validação Final

Execute este script para validar tudo:

```powershell
# 1. Verificar Python
Write-Host "1. Verificando Python..."
py --version
if ($LASTEXITCODE -ne 0) { Write-Error "Python não encontrado!" }

# 2. Verificar Whisper
Write-Host "2. Verificando Whisper..."
py -m whisper --version
if ($LASTEXITCODE -ne 0) { Write-Error "Whisper não encontrado!" }

# 3. Verificar FFmpeg
Write-Host "3. Verificando FFmpeg..."
ffmpeg -version | head -1
if ($LASTEXITCODE -ne 0) { Write-Error "FFmpeg não encontrado!" }

# 4. Compilar
Write-Host "4. Compilando..."
./gradlew clean build -x test
if ($LASTEXITCODE -ne 0) { Write-Error "Compilação falhou!" }

# 5. Criar arquivo de teste
Write-Host "5. Criando arquivo de teste..."
ffmpeg -f lavfi -i sine=frequency=1000:duration=5 -q:a 9 -acodec libmp3lame -b:a 32k test.mp3
ffmpeg -i test.mp3 test.ogg

# 6. Iniciar app
Write-Host "6. Iniciando aplicação..."
Start-Process -NoNewWindow -FilePath "./gradlew" -ArgumentList "bootRun"
Start-Sleep -Seconds 10

# 7. Testar endpoint
Write-Host "7. Testando endpoint..."
$file = Get-Item "test.ogg"
$form = @{ audio = $file }
$response = Invoke-WebRequest -Uri "http://localhost:8080/transcription" `
    -Method POST -Form $form -SkipHttpErrorCheck

Write-Host "Resposta:" $response.Content

# 8. Limpeza
Write-Host "Limpeza..."
Stop-Process -Name "java" -ErrorAction SilentlyContinue
Remove-Item "test.mp3" -ErrorAction SilentlyContinue
Remove-Item "test.ogg" -ErrorAction SilentlyContinue

Write-Host "✅ Validação completa!"
```

---

## 📞 Se Algo Não Funcionar

1. **Consulte ARCHITECTURE.md** - Troubleshooting detalhado
2. **Consulte TESTING_GUIDE.md** - Testes unitários
3. **Verifique os logs** - Ativar DEBUG logging
4. **Teste Whisper manualmente** - `py -m whisper audio.ogg`
5. **Verifique permissões** - Escrever em %TEMP%

---

**Data**: 17 de maio de 2026  
**Status**: ✅ Pronto para uso
