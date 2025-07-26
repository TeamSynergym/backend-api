package org.synergym.backendapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.synergym.backendapi.dto.ChatResponseDTO;
import java.util.Map;

@Service
public class AiCoachClient {
    private final WebClient webClient = WebClient.create("http://localhost:8000");

    public ChatResponseDTO sendAiCoachRequest(Map<String, Object> requestBody) {
        try {
            System.out.println("[DEBUG] === FastAPI /ai-coach 요청 시작 ===");
            System.out.println("[DEBUG] 요청 바디: " + requestBody);
            
            ChatResponseDTO response = webClient.post()
                    .uri("/ai-coach")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ChatResponseDTO.class)
                    .block();
            
            System.out.println("[DEBUG] FastAPI 응답 성공: " + response);
            return response;
            
        } catch (WebClientResponseException e) {
            System.out.println("[ERROR] === FastAPI 에러 발생 ===");
            System.out.println("[ERROR] HTTP 상태: " + e.getStatusCode());
            System.out.println("[ERROR] 에러 응답 본문: " + e.getResponseBodyAsString());
            System.out.println("[ERROR] 요청 바디: " + requestBody);
            throw e;
        } catch (Exception e) {
            System.out.println("[ERROR] 기타 에러: " + e.getMessage());
            throw e;
        }
    }
} 