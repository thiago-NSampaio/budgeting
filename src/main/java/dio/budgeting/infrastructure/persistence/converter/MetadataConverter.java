package dio.budgeting.infrastructure.persistence.converter;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dio.budgeting.domain.chatMemory.Metadata;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MetadataConverter implements AttributeConverter<Metadata, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @Override
    public String convertToDatabaseColumn(Metadata attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize metadata to JSON", e);
        }
    }

    @Override
    public Metadata convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return MAPPER.readValue(dbData, Metadata.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize metadata from JSON", e);
        }
    }
}