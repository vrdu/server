package com.example.server.controller;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.HarmCategory;
import com.google.cloud.vertexai.api.SafetySetting;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class LLMController {
    public GenerateContentResponse generateContent(String text, String projectId, String region) throws IOException {
        System.out.println("made it into generateContent");
        GenerateContentResponse response = null;

        try (VertexAI vertexAi = new VertexAI(projectId, region)) {
            GenerationConfig generationConfig =
                    GenerationConfig.newBuilder()
                            .setMaxOutputTokens(8192)
                            .setTemperature(1F)
                            .setTopP(0.95F)
                            .build();

            List<SafetySetting> safetySettings = Arrays.asList(
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
                            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                            .build()
            );

            GenerativeModel model =
                    new GenerativeModel.Builder()
                            .setModelName("gemini-1.5-pro-002")
                            .setVertexAi(vertexAi)
                            .setGenerationConfig(generationConfig)
                            .setSafetySettings(safetySettings)
                            .build();

            ChatSession chatSession = model.startChat();

            try {
                response = chatSession.sendMessage(text); // For multi-turn chat, use sendMessage.
                System.out.println("response: " + response);
            } catch (Exception e) {
                System.err.println("Error during content generation: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error during content generation: " + e.getMessage());
            e.printStackTrace();

            return response;
        }
        return response;
    }
}

