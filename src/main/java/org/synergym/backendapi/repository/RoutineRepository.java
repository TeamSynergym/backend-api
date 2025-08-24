package org.synergym.backendapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.synergym.backendapi.entity.Routine;
import org.synergym.backendapi.entity.User;

import java.util.List;

public interface RoutineRepository extends JpaRepository<Routine, Integer> {
    // 루틴 이름으로 검색
    List<Routine> findByName(String name);
    
    // 사용자별 루틴 조회 (기존 메서드)
    List<Routine> findByUser(User user);
    
    // 사용자별 루틴 조회 (N+1 문제 해결을 위한 최적화 메서드)
    @Query("SELECT DISTINCT r FROM Routine r " +
           "LEFT JOIN FETCH r.exercises re " +
           "LEFT JOIN FETCH re.exercise " +
           "WHERE r.user.id = :userId AND r.useYn = 'Y'")
    List<Routine> findByUserIdWithExercises(@Param("userId") int userId);
}