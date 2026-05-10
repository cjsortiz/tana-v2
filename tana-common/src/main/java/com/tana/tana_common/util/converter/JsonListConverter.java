package com.tana.tana_common.util.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
@Converter
public class JsonListConverter implements AttributeConverter<List<String>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) return "[]";
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(dbData, List.class);
        } catch (IOException e) {
            return new ArrayList<>();
        } catch (ClassCastException e) {
            // Handle case where JSON cannot be cast to Map<String,Object>
            throw new RuntimeException("Error casting JSON to Map<String,Object>: " + e.getMessage());
        }
    }
}