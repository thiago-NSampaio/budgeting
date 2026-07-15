package dio.budgeting.infrastructure.persistence.converter;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dio.budgeting.domain.chatMemory.ActionTaken;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ActionsConverter implements AttributeConverter<List<ActionTaken>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @Override
    public String convertToDatabaseColumn(List<ActionTaken> attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize actions", e);
        }
    }

    @Override
    public List<ActionTaken> convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not deserialize actions", e);
        }
    }
}
