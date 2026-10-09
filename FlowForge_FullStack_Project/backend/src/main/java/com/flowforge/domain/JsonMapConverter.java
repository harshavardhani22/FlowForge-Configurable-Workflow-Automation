package com.flowforge.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.LinkedHashMap;
import java.util.Map;

@Converter
public class JsonMapConverter implements AttributeConverter<Map<String,Object>, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public String convertToDatabaseColumn(Map<String,Object> value) {
        try { return MAPPER.writeValueAsString(value == null ? Map.of() : value); }
        catch (Exception e) { throw new IllegalArgumentException("Cannot serialize workflow context", e); }
    }
    public Map<String,Object> convertToEntityAttribute(String value) {
        try { return value == null || value.isBlank() ? new LinkedHashMap<>() : MAPPER.readValue(value, new TypeReference<>(){}); }
        catch (Exception e) { throw new IllegalArgumentException("Cannot parse workflow context", e); }
    }
}
