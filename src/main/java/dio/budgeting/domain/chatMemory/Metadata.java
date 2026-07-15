package dio.budgeting.domain.chatMemory;

import java.time.Instant;
import java.util.List;

public record Metadata(
    String interactionId,
    Instant timestamp,
    List<String> toolsUsed,
    long durationMs
) {}