package org.synergym.backendapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synergym.backendapi.dto.ExerciseDTO;
import org.synergym.backendapi.entity.Exercise;
import org.synergym.backendapi.exception.EntityNotFoundException;
import org.synergym.backendapi.exception.ErrorCode;
import org.synergym.backendapi.repository.ExerciseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {
    
    private final ExerciseRepository exerciseRepository;

    //ID로 운동 조회 (없으면 예외 발생)
    private Exercise findExerciseById(int id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EXERCISE_NOT_FOUND));
    }

    //운동 생성
    @Override
    @Transactional
    public Integer createExercise(ExerciseDTO exerciseDTO) {
        Exercise exercise = DTOtoEntity(exerciseDTO);
        Exercise savedExercise = exerciseRepository.save(exercise);
        return savedExercise.getId();
    }

    //전체 운동 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ExerciseDTO> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    //ID로 운동 단건 조회
    @Override
    public ExerciseDTO getExerciseById(Integer id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EXERCISE_NOT_FOUND));
        return entityToDTO(exercise);

    }

    //운동 삭제
    @Override
    @Transactional
    public void deleteExercise(Integer id) {
        Exercise exercise = findExerciseById(id);
        exerciseRepository.delete(exercise);
    }

    //이름으로 운동 검색
    @Override
    @Transactional(readOnly = true)
    public List<ExerciseDTO> getExercisesByName(String name) {
        List<ExerciseDTO> exercises = exerciseRepository.findByNameContaining(name)
                .stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
        
        // URL 정보 로그 출력
        for (ExerciseDTO exercise : exercises) {
            System.out.println("[DEBUG] Exercise 검색 결과 - Name: " + exercise.getName() + ", URL: " + exercise.getUrl());
            System.out.println("[DEBUG] Exercise 검색 결과 - Name: " + exercise.getName() + ", URL 타입: " + (exercise.getUrl() != null ? exercise.getUrl().getClass().getSimpleName() : "null"));
            System.out.println("[DEBUG] Exercise 검색 결과 - Name: " + exercise.getName() + ", URL is null: " + (exercise.getUrl() == null));
        }
        
        return exercises;
    }

    //카테고리로 운동 검색
    @Override
    @Transactional(readOnly = true)
    public List<ExerciseDTO> getExercisesByCategory(String category) {
        return exerciseRepository.findByCategory(category)
                .stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    //좋아요 수 기준 인기 운동 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ExerciseDTO> getPopularExercisesByLikes(int limit) {
        List<Exercise> exercises = exerciseRepository.findPopularExercisesByLikes(limit);
        return exercises.stream()
                .limit(limit)
                .map(exercise -> {
                    Long likeCount = exerciseRepository.countLikesByExerciseId(exercise.getId());
                    Long routineCount = exerciseRepository.countRoutinesByExerciseId(exercise.getId());
                    return entityToDTOWithStats(exercise, likeCount, routineCount);
                })
                .collect(Collectors.toList());
    }

    //루틴 포함 수 기준 인기 운동 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ExerciseDTO> getPopularExercisesByRoutines(int limit) {
        List<Exercise> exercises = exerciseRepository.findPopularExercisesByRoutines(limit);
        return exercises.stream()
                .limit(limit)
                .map(exercise -> {
                    Long likeCount = exerciseRepository.countLikesByExerciseId(exercise.getId());
                    Long routineCount = exerciseRepository.countRoutinesByExerciseId(exercise.getId());
                    return entityToDTOWithStats(exercise, likeCount, routineCount);
                })
                .collect(Collectors.toList());
    }
    
    //ID로 운동 단건 조회(좋아요/루틴 통계 포함)
    @Override
    @Transactional(readOnly = true)
    public ExerciseDTO getExerciseByIdWithStats(Integer id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EXERCISE_NOT_FOUND));
        
        Long likeCount = exerciseRepository.countLikesByExerciseId(exercise.getId());
        Long routineCount = exerciseRepository.countRoutinesByExerciseId(exercise.getId());
        
        return entityToDTOWithStats(exercise, likeCount, routineCount);
    }

    @Override
    public ExerciseDTO getExerciseByExactName(String name) {
        List<Exercise> exercises = exerciseRepository.findByName(name);
        System.out.println("[DEBUG] findByName('" + name + "') 결과 개수: " + (exercises == null ? 0 : exercises.size()));
        if (exercises == null || exercises.isEmpty()) {
            return null;
        }
        for (Exercise e : exercises) {
            System.out.println("[DEBUG] Exercise ID: " + e.getId() + ", Name: " + e.getName());
        }
        return entityToDTO(exercises.get(0));
    }
}