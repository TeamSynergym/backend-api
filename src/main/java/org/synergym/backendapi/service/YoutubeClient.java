package org.synergym.backendapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.synergym.backendapi.dto.ChatResponseDTO;
import java.util.Map;

@Service
public class YoutubeClient {
    private final WebClient webClient = WebClient.create("http://localhost:8000");

    public ChatResponseDTO sendYoutubeRequest(Map<String, Object> requestBody, String type) {
        requestBody.put("type", type);
        try {
            // 원본 JSON 및 파싱 결과 로그 제거
            webClient.post()
                    .uri("/youtube")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            ChatResponseDTO response = webClient.post()
                    .uri("/youtube")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ChatResponseDTO.class)
                    .block();
            return response;
        } catch (Exception e) {
            System.out.println("[YoutubeClient] FastAPI 호출 실패: " + e.getMessage());
            ChatResponseDTO errorResponse = new ChatResponseDTO();
            errorResponse.setType("error");
            errorResponse.setResponse("YouTube 서비스 호출 중 오류가 발생했습니다: " + e.getMessage());
            return errorResponse;
        }
    }
} 