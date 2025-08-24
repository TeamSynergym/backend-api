// package org.synergym.backendapi.util;

// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.core.io.ClassPathResource;
// import org.springframework.stereotype.Component;
// import org.synergym.backendapi.entity.Exercise;
// import org.synergym.backendapi.repository.ExerciseRepository;

// import javax.sql.DataSource;
// import java.io.IOException;
// import java.io.InputStream;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class ExerciseDataImporter implements CommandLineRunner {

//     private final ExerciseRepository exerciseRepository;
//     private final ObjectMapper objectMapper;
//     private final DataSource dataSource;

//     @Override
//     public void run(String... args) throws Exception {
//         try {
//             // 데이터베이스 스키마 수정 및 ID 시퀀스 리셋
//             fixDatabaseSchema();
            
//             // 운동 데이터 가져오기
//             importExerciseData();
            
//             log.info("Exercise data import completed successfully");
//         } catch (Exception e) {
//             log.error("Failed to import exercise data", e);
//         }
//     }

//     private void fixDatabaseSchema() {
//         try (Connection connection = dataSource.getConnection()) {
//             // 1. category 컬럼이 없는 경우 생성
//             if (!columnExists(connection, "Exercises", "category")) {
//                 log.info("Creating 'category' column");
//                 createCategoryColumn(connection);
//             }
            
//             // 2. url 컬럼이 없는 경우 생성
//             if (!columnExists(connection, "url")) {
//                 log.info("Creating 'url' column");
//                 createUrlColumn(connection);
//             }
            
//             // 3. ID 시퀀스 리셋 - ID가 1부터 시작하도록
//             resetIdSequence(connection);
            
//         } catch (Exception e) {
//             log.error("Failed to fix database schema: {}", e.getMessage());
//         }
//     }

//     private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
//         String sql = "SELECT column_name FROM information_schema.columns " +
//                     "WHERE table_name = ? AND column_name = ?";
//         try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//             stmt.setString(1, tableName);
//             stmt.setString(2, columnName);
//             try (ResultSet rs = stmt.executeQuery()) {
//                 return rs.next();
//             }
//         }
//     }

//     private boolean columnExists(Connection connection, String columnName) throws SQLException {
//         return columnExists(connection, "Exercises", columnName);
//     }

//     private void createCategoryColumn(Connection connection) throws SQLException {
//         String sql = "ALTER TABLE \"Exercises\" ADD COLUMN category VARCHAR(50)";
//         try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//             stmt.executeUpdate();
//             log.info("Created 'category' column in Exercises table");
//         }
//     }

//     private void createUrlColumn(Connection connection) throws SQLException {
//         String sql = "ALTER TABLE \"Exercises\" ADD COLUMN url VARCHAR(255)";
//         try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//             stmt.executeUpdate();
//             log.info("Created 'url' column in Exercises table");
//         }
//     }

//     private void resetIdSequence(Connection connection) throws SQLException {
//         try {
//             // PostgreSQL의 경우
//             String resetSequenceSQL = "ALTER SEQUENCE \"Exercises_exercise_id_seq\" RESTART WITH 1";
//             try (PreparedStatement stmt = connection.prepareStatement(resetSequenceSQL)) {
//                 stmt.executeUpdate();
//                 log.info("Reset PostgreSQL sequence to start from 1");
//             }
//         } catch (SQLException e) {
//             try {
//                 // H2의 경우
//                 String resetH2SQL = "ALTER TABLE \"Exercises\" ALTER COLUMN exercise_id RESTART WITH 1";
//                 try (PreparedStatement stmt = connection.prepareStatement(resetH2SQL)) {
//                     stmt.executeUpdate();
//                     log.info("Reset H2 sequence to start from 1");
//                 }
//             } catch (SQLException h2Error) {
//                 try {
//                     // MySQL의 경우
//                     String resetMySQLSQL = "ALTER TABLE Exercises AUTO_INCREMENT = 1";
//                     try (PreparedStatement stmt = connection.prepareStatement(resetMySQLSQL)) {
//                         stmt.executeUpdate();
//                         log.info("Reset MySQL AUTO_INCREMENT to start from 1");
//                     }
//                 } catch (SQLException mysqlError) {
//                     log.warn("Could not reset sequence for PostgreSQL, H2, or MySQL: {}", mysqlError.getMessage());
//                 }
//             }
//         }
//     }

//     private void importExerciseData() throws IOException {
//         log.info("Starting exercise data import...");
        
//         // naver_clean_ver3.json 파일 읽기
//         ClassPathResource resource = new ClassPathResource("seed/naver_clean_ver3.json");
        
//         try (InputStream inputStream = resource.getInputStream()) {
//             List<Map<String, Object>> exerciseDataList = objectMapper.readValue(
//                 inputStream, 
//                 new TypeReference<List<Map<String, Object>>>() {}
//             );
            
//             log.info("Loaded {} exercises from JSON file", exerciseDataList.size());
            
//             List<Exercise> exercisesToSave = new ArrayList<>();
//             int successCount = 0;
//             int failCount = 0;
            
//             for (Map<String, Object> exerciseData : exerciseDataList) {
//                 try {
//                     Exercise exercise = mapToExercise(exerciseData);
//                     if (exercise != null) {
//                         exercisesToSave.add(exercise);
//                         successCount++;
//                         log.debug("Mapped exercise: {} (category: {})", exercise.getName(), exercise.getCategory());
//                     }
//                 } catch (Exception e) {
//                     log.error("Failed to map exercise: {} - {}", exerciseData.get("name"), e.getMessage());
//                     failCount++;
//                 }
//             }

//             log.info("Mapping completed - Success: {}, Failed: {}", successCount, failCount);

//             // 배치로 저장
//             if (!exercisesToSave.isEmpty()) {
//                 List<Exercise> savedExercises = exerciseRepository.saveAll(exercisesToSave);
//                 log.info("Successfully saved {} exercises to database", savedExercises.size());
                
//                 // 카테고리별 통계 출력
//                 Map<String, Long> categoryStats = savedExercises.stream()
//                     .collect(java.util.stream.Collectors.groupingBy(
//                         ex -> ex.getCategory() != null ? ex.getCategory() : "Unknown",
//                         java.util.stream.Collectors.counting()
//                     ));
                
//                 log.info("Category distribution:");
//                 categoryStats.forEach((category, count) -> 
//                     log.info("  {}: {} exercises", category, count));
                
//                 // ID 범위 출력
//                 if (!savedExercises.isEmpty()) {
//                     int minId = savedExercises.stream().mapToInt(Exercise::getId).min().orElse(0);
//                     int maxId = savedExercises.stream().mapToInt(Exercise::getId).max().orElse(0);
//                     log.info("Exercise ID range: {} - {}", minId, maxId);
                    
//                     // 첫 번째와 마지막 운동 정보 출력
//                     Exercise firstExercise = savedExercises.stream()
//                         .filter(ex -> ex.getId() == minId)
//                         .findFirst()
//                         .orElse(null);
//                     Exercise lastExercise = savedExercises.stream()
//                         .filter(ex -> ex.getId() == maxId)
//                         .findFirst()
//                         .orElse(null);
                    
//                     if (firstExercise != null) {
//                         log.info("First exercise (ID {}): {} ({})", 
//                             firstExercise.getId(), firstExercise.getName(), firstExercise.getCategory());
//                     }
//                     if (lastExercise != null) {
//                         log.info("Last exercise (ID {}): {} ({})", 
//                             lastExercise.getId(), lastExercise.getName(), lastExercise.getCategory());
//                     }
//                 }
//             }
//         }
//     }

//     private Exercise mapToExercise(Map<String, Object> data) {
//         try {
//             // 필수 필드 검증
//             String name = (String) data.get("name");
//             if (name == null || name.trim().isEmpty()) {
//                 log.warn("Exercise name is null or empty, skipping");
//                 return null;
//             }

//             // category 필드 검증 및 기본값 설정
//             String category = (String) data.get("category");
//             if (category == null || category.trim().isEmpty()) {
//                 category = "기타"; // 기본값 설정
//             }

//             // bodyPart 필드 처리 (null일 수 있음)
//             String bodyPart = (String) data.get("bodyPart");
//             if (bodyPart != null && bodyPart.trim().isEmpty()) {
//                 bodyPart = null;
//             }

//             // posture 필드 처리 (null일 수 있음)
//             String posture = (String) data.get("posture");
//             if (posture != null && posture.trim().isEmpty()) {
//                 posture = null;
//             }

//             // url 필드 처리 (null일 수 있음)
//             String url = (String) data.get("url");
//             if (url != null && url.trim().isEmpty()) {
//                 url = null;
//             }

//             return Exercise.builder()
//                     .name(name.trim())
//                     .category(category.trim())  // JSON의 category를 Exercise의 category로 매핑
//                     .description((String) data.get("description"))
//                     .difficulty((String) data.get("difficulty"))
//                     .posture(posture)
//                     .bodyPart(bodyPart)
//                     .thumbnailUrl((String) data.get("thumbnail_url"))
//                     .url(url)
//                     .build();
//         } catch (Exception e) {
//             log.error("Error mapping exercise data: {}", data, e);
//             return null;
//         }
//     }
// }
