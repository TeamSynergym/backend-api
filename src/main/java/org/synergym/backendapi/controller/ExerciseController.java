package org.synergym.backendapi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synergym.backendapi.dto.ExerciseDTO;
import org.synergym.backendapi.dto.RecommendationPayloadDTO;
import org.synergym.backendapi.dto.RecommendationResponseDTO;
import org.synergym.backendapi.service.ExerciseService;
import org.synergym.backendapi.service.RecommendationWorkflowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final RecommendationWorkflowService recommendationWorkflowService;

    // 운동 전체 조회
    @GetMapping
    public ResponseEntity<List<ExerciseDTO>> getAllExercises() {
        List<ExerciseDTO> exercises = exerciseService.getAllExercises();
        return ResponseEntity.ok(exercises);
    }

    // 운동 단건 조회 (좋아요 수 포함)
    @GetMapping("/{exerciseId}")
    public ResponseEntity<ExerciseDTO> getExercise(@PathVariable int exerciseId) {
        ExerciseDTO exerciseDto = exerciseService.getExerciseByIdWithStats(exerciseId);
        return ResponseEntity.ok(exerciseDto);
    }

    // 운동 이름으로 검색 (좋아요 수 포함)
    @GetMapping("/search/name")
    public ResponseEntity<List<ExerciseDTO>> getExercisesByName(@RequestParam String name) {
        List<ExerciseDTO> exercises = exerciseService.getExercisesByName(name);
        System.out.println("[DEBUG] ExerciseController - getExercisesByName 호출: " + name);
        System.out.println("[DEBUG] 검색된 운동 수: " + exercises.size());
        for (ExerciseDTO exercise : exercises) {
            System.out.println("[DEBUG] 운동 정보 - Name: " + exercise.getName() + ", URL: " + exercise.getUrl());
        }
        return ResponseEntity.ok(exercises);
    }

    // 운동 카테고리로 검색
    @GetMapping("/search/category")
    public ResponseEntity<List<ExerciseDTO>> getExercisesByCategory(@RequestParam String category) {
        List<ExerciseDTO> exercises = exerciseService.getExercisesByCategory(category);
        return ResponseEntity.ok(exercises);
    }

    // 운동 생성
    @PostMapping
    public ResponseEntity<Integer> createExercise(@RequestBody ExerciseDTO exerciseDTO) {
        Integer id = exerciseService.createExercise(exerciseDTO);
        return ResponseEntity.ok(id);
    }

    // 운동 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable int id) {
        exerciseService.deleteExercise(id);
        return ResponseEntity.noContent().build();
    }

    // 좋아요 수 기준 인기 운동 조회
    @GetMapping("/popular/likes")
    public ResponseEntity<List<ExerciseDTO>> getPopularExercisesByLikes(@RequestParam(defaultValue = "10") int limit) {
        List<ExerciseDTO> exercises = exerciseService.getPopularExercisesByLikes(limit);
        return ResponseEntity.ok(exercises);
    }

    // 루틴 사용 횟수 기준 인기 운동 조회
    @GetMapping("/popular/routines")
    public ResponseEntity<List<ExerciseDTO>> getPopularExercisesByRoutines(@RequestParam(defaultValue = "10") int limit) {
        List<ExerciseDTO> exercises = exerciseService.getPopularExercisesByRoutines(limit);
        return ResponseEntity.ok(exercises);
    }
    
    // 좋아요 수가 포함된 운동 단건 조회
    @GetMapping("/{exerciseId}/with-stats")
    public ResponseEntity<ExerciseDTO> getExerciseWithStats(@PathVariable int exerciseId) {
        ExerciseDTO exerciseDto = exerciseService.getExerciseByIdWithStats(exerciseId);
        return ResponseEntity.ok(exerciseDto);
    }

    // 운동 이름과 정확히 일치 검색
    @GetMapping("/search/exact")
    public ResponseEntity<ExerciseDTO> getExerciseByExactName(@RequestParam String name) {
        try {
            ExerciseDTO exercise = exerciseService.getExerciseByExactName(name);
            if (exercise == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(exercise);
        } catch (Exception e) {
            // 로그 출력
            System.err.println("[ERROR] 운동명으로 검색 중 에러 발생: " + e.getMessage());
            e.printStackTrace();
            // 500 에러 대신 404 반환
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 사용자의 종합적인 데이터를 받아 AI 기반으로 운동을 추천합니다.
     * @param payloadDTO 사용자의 프로필, 운동 기록, 체형 분석 데이터 등
     * @return 추천된 운동 목록과 추천 이유
     */
    @PostMapping("/recommend-exercises")
    public ResponseEntity<RecommendationResponseDTO> recommendExercises(@RequestBody RecommendationPayloadDTO payloadDTO) {
        RecommendationResponseDTO response = recommendationWorkflowService.getRecommendations(payloadDTO);
        return ResponseEntity.ok(response);
    }
}
