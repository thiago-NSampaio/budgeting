package dio.budgeting.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dio.budgeting.domain.chatMemory.RequestConfirmation;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ConfirmationConverter implements AttributeConverter<RequestConfirmation, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @Override
    public String convertToDatabaseColumn(RequestConfirmation attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize confirmation", e);
        }
    }

    @Override
    public RequestConfirmation convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return objectMapper.readValue(dbData, RequestConfirmation.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not deserialize confirmation", e);
        }
    }
}
