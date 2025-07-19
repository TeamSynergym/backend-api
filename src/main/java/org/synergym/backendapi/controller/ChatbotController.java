package org.synergym.backendapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.synergym.backendapi.dto.ChatRequestDTO;
import org.synergym.backendapi.dto.ChatResponseDTO;
import org.synergym.backendapi.dto.ChatMessageDTO;
import org.synergym.backendapi.service.AiChatbotService;
import org.synergym.backendapi.service.ChatbotRedisService;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {
    private final AiChatbotService aiChatbotService;
    private final ChatbotRedisService chatbotRedisService;

    // 메시지 전송 + FastAPI 호출 + Redis 저장
    @PostMapping("/send")
    public ResponseEntity<ChatResponseDTO> sendMessage(@RequestBody ChatRequestDTO requestDTO) {
        try {
            Integer userId = requestDTO.getUserId();
            String sessionId = chatbotRedisService.getActiveSession(userId);
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = chatbotRedisService.generateSessionId();
                chatbotRedisService.setActiveSession(userId, sessionId);
            }

            // 1. 사용자 메시지 Redis에 저장
            if (requestDTO.getMessage() != null && !requestDTO.getMessage().isEmpty()) {
                chatbotRedisService.saveChatMessage(userId, sessionId, requestDTO.getMessage(), null);
            }

            // 2. FastAPI 호출
            ChatResponseDTO aiResponse = aiChatbotService.callFastApi(requestDTO);

            // 3. AI 응답 Redis에 저장 (비디오 URL이 있는 경우 포함)
            if (aiResponse.getResponse() != null && !aiResponse.getResponse().isEmpty()) {
                if (aiResponse.getVideoUrl() != null && !aiResponse.getVideoUrl().isEmpty()) {
                    // 비디오 URL이 있는 경우 특별 처리
                    chatbotRedisService.forceAddMessage(userId, sessionId, aiResponse.getResponse(), "video", aiResponse.getVideoUrl());
                } else {
                    chatbotRedisService.saveChatMessage(userId, sessionId, null, aiResponse.getResponse());
                }
            }

            // 4. 응답 반환 (세션ID도 포함)
            aiResponse.setSessionId(sessionId);
            return ResponseEntity.ok(aiResponse);
        } catch (Exception e) {
            // 에러 발생 시 기본 응답 반환
            ChatResponseDTO errorResponse = new ChatResponseDTO();
            errorResponse.setType("error");
            errorResponse.setResponse("챗봇 응답 중 오류가 발생했습니다. 다시 시도해 주세요.");
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
            String sessionId = chatbotRedisService.getActiveSession(userId);
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = chatbotRedisService.generateSessionId();
                chatbotRedisService.setActiveSession(userId, sessionId);
            }

            // 1. 사용자 메시지 Redis에 저장
            if (requestDTO.getMessage() != null && !requestDTO.getMessage().isEmpty()) {
                chatbotRedisService.saveChatMessage(userId, sessionId, requestDTO.getMessage(), null);
            }

            // 2. FastAPI 댓글 요약 엔드포인트 호출
            ChatResponseDTO aiResponse = aiChatbotService.callFastApiCommentSummary(requestDTO);

            // 3. AI 응답 Redis에 저장
            if (aiResponse.getResponse() != null && !aiResponse.getResponse().isEmpty()) {
                chatbotRedisService.saveChatMessage(userId, sessionId, null, aiResponse.getResponse());
            }

            // 4. 응답 반환 (세션ID도 포함)
            aiResponse.setSessionId(sessionId);
            return ResponseEntity.ok(aiResponse);
        } catch (Exception e) {
            // 에러 발생 시 기본 응답 반환
            ChatResponseDTO errorResponse = new ChatResponseDTO();
            errorResponse.setType("error");
            errorResponse.setResponse("댓글 요약 중 오류가 발생했습니다. 다시 시도해 주세요.");
            return ResponseEntity.ok(errorResponse);
        }
    }
}
