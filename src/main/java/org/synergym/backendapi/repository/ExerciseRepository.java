package org.synergym.backendapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.synergym.backendapi.entity.Exercise;

public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {
    
    // 운동 이름으로 검색 (부분 일치)
    List<Exercise> findByNameContaining(String name);
    
    // 운동 카테고리별 조회
    List<Exercise> findByCategory(String category);
    
    // 운동 이름과 정확히 일치
    List<Exercise> findByName(String name);
    
    // 좋아요 수 기준 인기 운동 조회
    @Query("SELECT e FROM Exercise e " +
           "LEFT JOIN ExerciseLike el ON e.id = el.exercise.id " +
           "GROUP BY e.id " +
           "ORDER BY COUNT(el.exercise.id) DESC")
    List<Exercise> findPopularExercisesByLikes(@Param("limit") int limit);
    
    // 루틴 사용 횟수 기준 인기 운동 조회
    @Query("SELECT e FROM Exercise e " +
           "LEFT JOIN RoutineExercise re ON e.id = re.exercise.id " +
           "GROUP BY e.id " +
           "ORDER BY COUNT(re.exercise.id) DESC")
    List<Exercise> findPopularExercisesByRoutines(@Param("limit") int limit);
    
    // 특정 운동의 좋아요 수 조회
    @Query("SELECT COUNT(el) FROM ExerciseLike el WHERE el.exercise.id = :exerciseId")
    Long countLikesByExerciseId(@Param("exerciseId") Integer exerciseId);
    
    // 특정 운동의 루틴 사용 횟수 조회
    @Query("SELECT COUNT(re) FROM RoutineExercise re WHERE re.exercise.id = :exerciseId")
    Long countRoutinesByExerciseId(@Param("exerciseId") Integer exerciseId);
}
