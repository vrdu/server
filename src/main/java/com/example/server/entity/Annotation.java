package com.example.server.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Embeddable
public class Annotation {

    private int annotationId;
    @Lob
    private String jsonData;

    public Annotation() {}

    public Annotation(String jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public String toString() {
        return jsonData;
    }

    /**
     * Parses the JSON data to the desired key-value pair format.
     * Example output: [{labelName1: text1}, {labelName2: text2}]
     * New desired output: {labelName1:text1, labelName2: text2}
     */
    public String parseAnnotations() {
        if (jsonData == null || jsonData.isEmpty()) {
            return "{}"; // Return an empty object for empty or null jsonData
        }

        try {
            // Use Jackson ObjectMapper to parse the JSON string
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse jsonData into a list of maps
            List<Map<String, Object>> annotations = objectMapper.readValue(jsonData, new TypeReference<>() {});

            // Build the desired output
            StringBuilder output = new StringBuilder("{");
            for (Map<String, Object> annotation : annotations) {
                String labelName = (String) annotation.get("labelName");
                String text = (String) annotation.get("text");
                output.append("\"").append(labelName).append("\"").append(": ").append("\"").append(text).append("\"").append(", ");
            }

            // Remove the trailing comma and space, and close the JSON object
            if (output.length() > 1) {
                output.setLength(output.length() - 2);
            }
            output.append("}");

            return output.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing annotations: " + e.getMessage(), e);
        }
    }
    /*
    public String parseAnnotations() {
        if (jsonData == null || jsonData.isEmpty()) {
            return "[]"; // Return an empty array for empty or null jsonData
        }

        try {
            // Use Jackson ObjectMapper to parse the JSON string
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse jsonData into a list of maps
            List<Map<String, Object>> annotations = objectMapper.readValue(jsonData, new TypeReference<>() {});

            // Build the desired output
            StringBuilder output = new StringBuilder("[");
            for (Map<String, Object> annotation : annotations) {
                String labelName = (String) annotation.get("labelName");
                String text = (String) annotation.get("text");
                output.append("{")
                        .append(labelName).append(": ").append(text)
                        .append("}, ");
            }

            // Remove the trailing comma and space, and close the JSON array
            if (output.length() > 1) {
                output.setLength(output.length() - 2);
            }
            output.append("]");

            return output.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing annotations: " + e.getMessage(), e);
        }
    }*/
    public String parseAnnotationsToNullValues() {
        if (jsonData == null || jsonData.isEmpty()) {
            return "{}"; // Return an empty object for empty or null jsonData
        }

        try {
            // Use Jackson ObjectMapper to parse the JSON string
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse jsonData into a list of maps
            List<Map<String, Object>> annotations = objectMapper.readValue(jsonData, new TypeReference<>() {});

            // Build the desired output
            StringBuilder output = new StringBuilder("{");
            for (Map<String, Object> annotation : annotations) {
                String labelName = (String) annotation.get("labelName");
                output.append("\"").append(labelName).append("\"").append(": null, ");
            }

            // Remove the trailing comma and space, and close the JSON object
            if (output.length() > 1) {
                output.setLength(output.length() - 2);
            }
            output.append("}");

            return output.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing annotations: " + e.getMessage(), e);
        }
    }
    /**
     * Parses the JSON data to the desired key-value pair format.
     * Example output: [{labelName1: null}, {labelName2: null}]
     * New desired output: {labelName1:null, labelName2: null}
     */
    /*
    public String parseAnnotationsToNullValues() {
        if (jsonData == null || jsonData.isEmpty()) {
            return "[]"; // Return an empty array for empty or null jsonData
        }

        try {
            // Use Jackson ObjectMapper to parse the JSON string
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse jsonData into a list of maps
            List<Map<String, Object>> annotations = objectMapper.readValue(jsonData, new TypeReference<>() {});

            // Build the desired output
            StringBuilder output = new StringBuilder("[");
            for (Map<String, Object> annotation : annotations) {
                String labelName = (String) annotation.get("labelName");
                output.append("{")
                        .append(labelName).append(": null")
                        .append("}, ");
            }

            // Remove the trailing comma and space, and close the JSON array
            if (output.length() > 1) {
                output.setLength(output.length() - 2);
            }
            output.append("]");

            return output.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing annotations: " + e.getMessage(), e);
        }
    }
*/
}
