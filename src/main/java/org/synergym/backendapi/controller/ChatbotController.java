package org.synergym.backendapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.synergym.backendapi.dto.ChatRequestDTO;
import org.synergym.backendapi.dto.ChatResponseDTO;
import org.synergym.backendapi.dto.ChatMessageDTO;
import org.synergym.backendapi.service.ChatbotRedisService;
import org.synergym.backendapi.dto.AnalysisHistoryDTO;
import org.synergym.backendapi.service.AnalysisHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.synergym.backendapi.service.AiCoachClient;
import org.synergym.backendapi.service.YoutubeClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {
    private final AiCoachClient aiCoachClient;
    private final YoutubeClient youtubeClient;
    private final ChatbotRedisService chatbotRedisService;
    private final AnalysisHistoryService analysisHistoryService;

    // 메시지 전송 + FastAPI 호출 + Redis 저장
    @PostMapping("/send")
    public ResponseEntity<ChatResponseDTO> sendMessage(@RequestBody ChatRequestDTO requestDTO) {
        try {
            Integer userId = requestDTO.getUserId();
            Integer historyId = requestDTO.getHistoryId();
            String type = requestDTO.getType();

            // 1. 분석 이력 조회
            AnalysisHistoryDTO analysis = analysisHistoryService.getAnalysisHistoryById(historyId);

            // 2. 진단/추천운동 추출
            String diagnosis = analysis.getDiagnosis(); // JSON string
            Map<String, Object> recommendedExercise = getRecommendedExerciseFromAnalysis(analysis);

            // 2-1. diagnosis가 JSON 문자열이면 Map으로 변환
            Map<String, Object> diagnosisMap = null;
            try {
                if (diagnosis != null) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    diagnosisMap = objectMapper.readValue(diagnosis, Map.class);
                }
            } catch (Exception e) {
                System.out.println("[ERROR] diagnosis JSON 파싱 실패: " + e.getMessage());
                diagnosisMap = new java.util.HashMap<>();
            }

            // 3. FastAPI 요청용 body 생성 (snake_case로 맞춤)
            Map<String, Object> fastApiRequest = new java.util.HashMap<>();
            fastApiRequest.put("userId", userId);
            fastApiRequest.put("historyId", historyId);
            fastApiRequest.put("diagnosis", diagnosisMap);
            fastApiRequest.put("recommended_exercise", recommendedExercise);
            fastApiRequest.put("message", requestDTO.getMessage());

            System.out.println("[DEBUG] FastAPI 요청 바디: " + fastApiRequest);

            // 4. FastAPI 호출
            ChatResponseDTO aiResponse;
            if ("recommend".equals(type) || "summary".equals(type) || "comment_summary".equals(type)) {
                aiResponse = youtubeClient.sendYoutubeRequest(fastApiRequest, type);
                System.out.println("[DEBUG] YouTube 응답 - SessionId: " + aiResponse.getSessionId() + ", Response: " + aiResponse.getResponse());
            } else {
                aiResponse = aiCoachClient.sendAiCoachRequest(fastApiRequest);
                System.out.println("[DEBUG] AI Coach 응답 - SessionId: " + aiResponse.getSessionId() + ", Response: " + aiResponse.getResponse());
            }
            
            // 최종 ChatResponseDTO 출력 로그 제거

            // 6. 응답 반환 (세션ID도 포함)
            // Redis 저장 전에 세션ID 보장
            String sessionId = chatbotRedisService.getActiveSession(userId);
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = chatbotRedisService.generateSessionId();
                chatbotRedisService.setActiveSession(userId, sessionId);
            }
            aiResponse.setSessionId(sessionId);

            // Redis 저장
            if (aiResponse.getResponse() != null && !aiResponse.getResponse().isEmpty()) {
                if (aiResponse.getVideoUrl() != null && !aiResponse.getVideoUrl().isEmpty()) {
                    System.out.println("[DEBUG] Redis에 forceAddMessage 저장: userId=" + userId + ", sessionId=" + aiResponse.getSessionId() + ", response=" + aiResponse.getResponse() + ", videoUrl=" + aiResponse.getVideoUrl());
                    chatbotRedisService.forceAddMessage(userId, aiResponse.getSessionId(), aiResponse.getResponse(), "video", aiResponse.getVideoUrl());
                } else {
                    System.out.println("[DEBUG] Redis에 saveChatMessage 저장: userId=" + userId + ", sessionId=" + aiResponse.getSessionId() + ", response=" + aiResponse.getResponse());
                    chatbotRedisService.saveChatMessage(userId, aiResponse.getSessionId(), requestDTO.getMessage(), aiResponse.getResponse());
                }
            }
            
            // YouTube 관련 정보 로깅
            if ("recommend".equals(type) || "comment_summary".equals(type)) {
                System.out.println("[DEBUG] YouTube 응답 정보:");
                System.out.println("  - Type: " + aiResponse.getType());
                System.out.println("  - Video URL: " + aiResponse.getVideoUrl());
                System.out.println("  - Comment Count: " + aiResponse.getCommentCount());
                System.out.println("  - YouTube Summary: " + aiResponse.getYoutubeSummary());
            }
            
            // Spring에서 실제 JSON 직렬화 결과 확인
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(aiResponse);
                System.out.println("[DEBUG] Spring에서 직렬화된 JSON: " + jsonString);
            } catch (Exception e) {
                System.out.println("[ERROR] JSON 직렬화 실패: " + e.getMessage());
            }
            
            return ResponseEntity.ok(aiResponse);
        } catch (Exception e) {
            ChatResponseDTO errorResponse = new ChatResponseDTO();
            errorResponse.setType("error");
            errorResponse.setResponse("챗봇 응답 중 오류가 발생했습니다. 다시 시도해 주세요.\n" + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    // 대화 이력 조회 (현재 활성 세션)
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatMessageDTO>> getHistory(@PathVariable Integer userId) {
        try {
            String sessionId = chatbotRedisService.getActiveSession(userId);
            if (sessionId == null || sessionId.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            List<ChatMessageDTO> history = chatbotRedisService.getChatHistory(userId, sessionId);
            return ResponseEntity.ok(history != null ? history : List.of());
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    // 대화 이력 조회 (특정 세션)
    @GetMapping("/history/{userId}/{sessionId}")
    public ResponseEntity<List<ChatMessageDTO>> getHistoryBySession(
            @PathVariable Integer userId,
            @PathVariable String sessionId) {
        try {
            List<ChatMessageDTO> history = chatbotRedisService.getChatHistory(userId, sessionId);
            return ResponseEntity.ok(history != null ? history : List.of());
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    // 세션ID 조회
    @GetMapping("/active-session/{userId}")
    public ResponseEntity<String> getActiveSession(@PathVariable Integer userId) {
        try {
            String sessionId = chatbotRedisService.getActiveSession(userId);
            return ResponseEntity.ok(sessionId != null ? sessionId : "");
        } catch (Exception e) {
            return ResponseEntity.ok("");
        }
    }

    // 새로운 세션 시작
    @PostMapping("/new-session/{userId}")
    public ResponseEntity<String> startNewSession(@PathVariable Integer userId) {
        try {
            String sessionId = chatbotRedisService.generateSessionId();
            chatbotRedisService.setActiveSession(userId, sessionId);
            return ResponseEntity.ok(sessionId);
        } catch (Exception e) {
            return ResponseEntity.ok("");
        }
    }

    // 댓글 요약 요청
    @PostMapping("/comment-summary")
    public ResponseEntity<ChatResponseDTO> requestCommentSummary(@RequestBody ChatRequestDTO requestDTO) {
        try {
            Integer userId = requestDTO.getUserId();
            Integer historyId = requestDTO.getHistoryId();
            
            // 1. 분석 이력 조회
            AnalysisHistoryDTO analysis = analysisHistoryService.getAnalysisHistoryById(historyId);
            
            // 2. 진단/추천운동 추출
            String diagnosis = analysis.getDiagnosis();
            Map<String, Object> recommendedExercise = getRecommendedExerciseFromAnalysis(analysis);
            
            // 2-1. diagnosis가 JSON 문자열이면 Map으로 변환
            Map<String, Object> diagnosisMap = null;
            try {
                if (diagnosis != null) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    diagnosisMap = objectMapper.readValue(diagnosis, Map.class);
                }
            } catch (Exception e) {
                System.out.println("[ERROR] diagnosis JSON 파싱 실패: " + e.getMessage());
                diagnosisMap = new java.util.HashMap<>();
            }
            
            // 3. FastAPI 요청용 body 생성
            Map<String, Object> fastApiRequest = new java.util.HashMap<>();
            fastApiRequest.put("userId", userId);
            fastApiRequest.put("historyId", historyId);
            fastApiRequest.put("message", requestDTO.getMessage());
            fastApiRequest.put("diagnosis", diagnosisMap);
            fastApiRequest.put("recommended_exercise", recommendedExercise);
            
            System.out.println("[DEBUG] 댓글 요약 FastAPI 요청: " + fastApiRequest);
            
            // 4. FastAPI 댓글 요약 엔드포인트 호출 (type: 'comment_summary')
            ChatResponseDTO aiResponse = youtubeClient.sendYoutubeRequest(fastApiRequest, "comment_summary");
            
            // 5. 세션ID 설정
            String sessionId = chatbotRedisService.getActiveSession(userId);
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = chatbotRedisService.generateSessionId();
                chatbotRedisService.setActiveSession(userId, sessionId);
            }
            aiResponse.setSessionId(sessionId);
            
            // 6. AI 응답 Redis에 저장
            if (aiResponse.getResponse() != null && !aiResponse.getResponse().isEmpty()) {
                System.out.println("[DEBUG] Redis 저장 시도 - UserId: " + userId + ", SessionId: " + aiResponse.getSessionId());
                chatbotRedisService.forceAddMessage(userId, aiResponse.getSessionId(), aiResponse.getResponse(), "bot", "text");
                System.out.println("[DEBUG] Redis 저장 완료");
            }
            
            // 7. YouTube 관련 정보 로깅
            System.out.println("[DEBUG] 댓글 요약 응답 정보:");
            System.out.println("  - Type: " + aiResponse.getType());
            System.out.println("  - Video URL: " + aiResponse.getVideoUrl());
            System.out.println("  - Comment Count: " + aiResponse.getCommentCount());
            System.out.println("  - YouTube Summary: " + aiResponse.getYoutubeSummary());
            
            return ResponseEntity.ok(aiResponse);
        } catch (Exception e) {
            System.out.println("[ERROR] 댓글 요약 요청 실패: " + e.getMessage());
            ChatResponseDTO errorResponse = new ChatResponseDTO();
            errorResponse.setType("error");
            errorResponse.setResponse("댓글 요약 중 오류가 발생했습니다. 다시 시도해 주세요.\n" + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    // 추천운동 추출 예시 (실제 로직에 맞게 구현)
    private Map<String, Object> getRecommendedExerciseFromAnalysis(AnalysisHistoryDTO analysis) {
        return analysis.getRecommendedExercise();
    }
}
