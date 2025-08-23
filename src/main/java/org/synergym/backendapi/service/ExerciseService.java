package org.synergym.backendapi.service;

import org.synergym.backendapi.dto.ExerciseDTO;
import org.synergym.backendapi.entity.Exercise;

import java.util.List;

public interface ExerciseService {

    // 운동 생성
    Integer createExercise(ExerciseDTO exerciseDTO);

    // 모든 운동 조회
    List<ExerciseDTO> getAllExercises();

    // ID로 운동 조회
    ExerciseDTO getExerciseById(Integer id);

    // 운동 삭제
    void deleteExercise(Integer id);

    // 운동 이름으로 검색
    List<ExerciseDTO> getExercisesByName(String name);

    // 운동 카테고리별 조회
    List<ExerciseDTO> getExercisesByCategory(String category);

    // 좋아요 수 기준 인기 운동 조회
    List<ExerciseDTO> getPopularExercisesByLikes(int limit);

    // 루틴 사용 횟수 기준 인기 운동 조회
    List<ExerciseDTO> getPopularExercisesByRoutines(int limit);

    ExerciseDTO getExerciseByIdWithStats(Integer id);

    // 운동 이름과 정확히 일치
    ExerciseDTO getExerciseByExactName(String name);

    // DTO -> Entity 변환
    default Exercise DTOtoEntity(ExerciseDTO dto) {
        return Exercise.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .difficulty(dto.getDifficulty())
                .posture(dto.getPosture())
                .bodyPart(dto.getBodyPart())
                .thumbnailUrl(dto.getThumbnailUrl())
                .url(dto.getUrl())
                .build();
    }

    // Entity -> DTO 변환
    default ExerciseDTO entityToDTO(Exercise exercise) {
        return ExerciseDTO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .category(exercise.getCategory())
                .description(exercise.getDescription())
                .difficulty(exercise.getDifficulty())
                .posture(exercise.getPosture())
                .bodyPart(exercise.getBodyPart())
                .thumbnailUrl(exercise.getThumbnailUrl())
                .url(exercise.getUrl())
                .createdAt(exercise.getCreatedAt())
                .updatedAt(exercise.getUpdatedAt())
                .useYn(exercise.getUseYn())
                .build();
    }
    
    // Entity -> DTO 변환 (통계 정보 포함)
    default ExerciseDTO entityToDTOWithStats(Exercise exercise, Long likeCount, Long routineCount) {
        return ExerciseDTO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .category(exercise.getCategory())
                .description(exercise.getDescription())
                .difficulty(exercise.getDifficulty())
                .posture(exercise.getPosture())
                .bodyPart(exercise.getBodyPart())
                .thumbnailUrl(exercise.getThumbnailUrl())
                .url(exercise.getUrl())
                .createdAt(exercise.getCreatedAt())
                .updatedAt(exercise.getUpdatedAt())
                .useYn(exercise.getUseYn())
                .likeCount(likeCount)
                .routineCount(routineCount)
                .build();
    }
}
