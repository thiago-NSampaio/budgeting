package dio.budgeting.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dio.budgeting.domain.chatMemory.Error;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ErrorConverter implements AttributeConverter<Error, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @Override
    public String convertToDatabaseColumn(Error attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize error", e);
        }
    }

    @Override
    public Error convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return objectMapper.readValue(dbData, Error.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not deserialize error", e);
        }
    }
}
