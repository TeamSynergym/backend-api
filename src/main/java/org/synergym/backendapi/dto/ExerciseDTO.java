package org.synergym.backendapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Data  // getter, setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor  // 기본 생성자 (Jackson 직렬화용)
@AllArgsConstructor  // 모든 필드 생성자 (@Builder와 함께 사용)
@Builder  // 빌더 패턴 지원

// 운동 관련 DTO
public class ExerciseDTO {

    // Response용 필드 (조회 시에만 사용)
    private Integer id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Character useYn;

    // Request/Response 공통 필드
    private String name;
    private String category;  // 카테고리
    private String description;  // 설명
    private String difficulty;  // 난이도
    private String posture;  // 자세(선자세, 앉은자세, 누운자세)
    private String bodyPart;  // 부위 (camelCase로 수정)
    @JsonProperty("url")
    private String url;  // 상세 정보 URL
    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;  // 썸네일 url (camelCase로 수정)
    
    // 통계 필드 (인기 운동 조회 시 사용)
    private Long likeCount;  // 좋아요 수
    private Long routineCount;  // 루틴 사용 횟수
}