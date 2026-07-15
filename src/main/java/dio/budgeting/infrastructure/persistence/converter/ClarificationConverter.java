package dio.budgeting.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dio.budgeting.domain.chatMemory.Clarification;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ClarificationConverter implements AttributeConverter<Clarification, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @Override
    public String convertToDatabaseColumn(Clarification attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize clarification", e);
        }
    }

    @Override
    public Clarification convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return objectMapper.readValue(dbData, Clarification.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not deserialize clarification", e);
        }
    }
}
