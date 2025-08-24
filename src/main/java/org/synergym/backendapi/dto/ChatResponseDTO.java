package org.synergym.backendapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private String type;  // "bot", "error", "recommend", "comment_summary", "ai_coach"
    private String response;
    private String sessionId;
    private String videoUrl;
    private String videoTitle;
    private Map<String, Object> youtubeSummary;  // YouTube 요약 정보
    private Integer commentCount;  // 댓글 수
    
    // AI 코치 응답을 위한 운동 정보 필드들
    private Map<String, Object> exerciseInfo;  // 운동 정보 (썸네일, 설명, URL 등)
}
