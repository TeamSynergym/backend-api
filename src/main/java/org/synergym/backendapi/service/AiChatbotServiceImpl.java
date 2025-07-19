package org.synergym.backendapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.synergym.backendapi.dto.ChatRequestDTO;
import org.synergym.backendapi.dto.ChatResponseDTO;

@Service
public class AiChatbotServiceImpl implements AiChatbotService {
    private final WebClient webClient = WebClient.create("http://localhost:8000"); // FastAPI 주소

    @Override
    public ChatResponseDTO callFastApi(ChatRequestDTO requestDTO) {
        return webClient.post()
                .uri("/chatbot")
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(ChatResponseDTO.class)
                .block();
    }

    @Override
    public ChatResponseDTO callFastApiCommentSummary(ChatRequestDTO requestDTO) {
        return webClient.post()
                .uri("/chatbot/comment-summary")
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(ChatResponseDTO.class)
                .block();
    }
} 