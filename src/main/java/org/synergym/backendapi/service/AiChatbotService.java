package org.synergym.backendapi.service;

import org.synergym.backendapi.dto.ChatRequestDTO;
import org.synergym.backendapi.dto.ChatResponseDTO;

public interface AiChatbotService {
    ChatResponseDTO callFastApi(ChatRequestDTO requestDTO);
    ChatResponseDTO callFastApiCommentSummary(ChatRequestDTO requestDTO);
} 