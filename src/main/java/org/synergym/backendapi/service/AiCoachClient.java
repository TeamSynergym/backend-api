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
            
            // FastAPI 응답을 Map으로 받아서 ChatResponseDTO로 변환
            Map<String, Object> fastApiResponse = webClient.post()
                    .uri("/ai-coach")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            System.out.println("[DEBUG] FastAPI 응답 성공: " + fastApiResponse);
            
            // Map 응답을 ChatResponseDTO로 변환
            ChatResponseDTO response = new ChatResponseDTO();
            response.setType("ai_coach"); // AI 코치 응답 타입으로 고정
            response.setResponse((String) fastApiResponse.get("response"));
            
            // exercise_info가 있는 경우 설정
            if (fastApiResponse.containsKey("exercise_info")) {
                Map<String, Object> exerciseInfo = (Map<String, Object>) fastApiResponse.get("exercise_info");
                response.setExerciseInfo(exerciseInfo);
                System.out.println("[DEBUG] exercise_info 파싱 완료: " + exerciseInfo);
                System.out.println("[DEBUG] - thumbnail_url: " + exerciseInfo.get("thumbnail_url"));
                System.out.println("[DEBUG] - url: " + exerciseInfo.get("url"));
            } else {
                System.out.println("[DEBUG] exercise_info가 FastAPI 응답에 없음");
            }
            
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