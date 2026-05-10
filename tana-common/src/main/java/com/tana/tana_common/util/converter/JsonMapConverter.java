package com.tana.tana_common.util.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Converter
public class JsonMapConverter implements AttributeConverter<Map<String,Object>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String,Object> attribute) {
        if (attribute == null) return "{}";
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert map to JSON", e);
        }
    }

    @Override
    public Map<String,Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return new HashMap<>();
        try {
            return (Map <String,Object>)objectMapper.readValue(dbData, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not convert JSON to map", e);
        } catch (ClassCastException e) {
            // Handle case where JSON cannot be cast to Map<String,Object>
            throw new RuntimeException("Error casting JSON to Map<String,Object>: " + e.getMessage());
        }
    }
}
