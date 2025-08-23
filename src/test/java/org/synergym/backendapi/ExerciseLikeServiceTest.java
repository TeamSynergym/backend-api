package org.synergym.backendapi;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synergym.backendapi.dto.ExerciseLikeDTO;
import org.synergym.backendapi.entity.Exercise;
import org.synergym.backendapi.entity.User;
import org.synergym.backendapi.exception.EntityNotFoundException;
import org.synergym.backendapi.repository.ExerciseLikeRepository;
import org.synergym.backendapi.repository.ExerciseRepository;
import org.synergym.backendapi.repository.UserRepository;
import org.synergym.backendapi.service.ExerciseLikeService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExerciseLikeServiceTest {

    @Autowired
    private ExerciseLikeService exerciseLikeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private ExerciseLikeRepository exerciseLikeRepository;

    private static User testUser;
    private static Exercise testExercise;
    private static ExerciseLikeDTO testLikeDTO;

    @BeforeAll
    static void setUp() {
        System.out.println("=== ExerciseLikeService Test Start ===");
    }

    @BeforeEach
    void createTestData() {
        // Create test user
        testUser = userRepository.save(
                User.builder()
                        .email("testuser@test.com")
                        .name("TestUser")
                        .password("test123")
                        .goal("Health Management")
                        .build()
        );

        // Create test exercise
        testExercise = exerciseRepository.save(
                Exercise.builder()
                .name("Squat")
                .category("Strength Training")  // Add category field
                .difficulty("Beginner")
                .bodyPart("Lower Body")
                .description("Lower body strength exercise")
                .thumbnailUrl("https://example.com/squat.jpg")
                .url("https://example.com/squat")
                .build()
        );

        // Create test DTO
        testLikeDTO = ExerciseLikeDTO.builder()
                .userId(testUser.getId())
                .exerciseId(testExercise.getId())
                .build();

        System.out.println("Test data created:");
        System.out.println("- User ID: " + testUser.getId() + ", Name: " + testUser.getName());
        System.out.println("- Exercise ID: " + testExercise.getId() + ", Name: " + testExercise.getName());
    }

    @AfterEach
    void cleanUp() {
        // Clean up test data
        exerciseLikeRepository.deleteAll();
        exerciseRepository.deleteAll();
        userRepository.deleteAll();
        System.out.println("Test data cleaned up\n");
    }

    @Test
    @Order(1)
    @DisplayName("Add Like Test")
    void add() {
        System.out.println("--- Add Like Test ---");
        
        // Check state before adding
        boolean beforeAdd = exerciseLikeService.isLiked(testUser.getId(), testExercise.getId());
        System.out.println("State before adding: " + beforeAdd);
        assertFalse(beforeAdd);

        // Add like
        exerciseLikeService.add(testLikeDTO);
        System.out.println("Like added");

        // Check state after adding
        boolean afterAdd = exerciseLikeService.isLiked(testUser.getId(), testExercise.getId());
        System.out.println("State after adding: " + afterAdd);
        assertTrue(afterAdd);

        // Check user's likes list
        var userLikes = exerciseLikeService.getByUser(testUser.getId());
        System.out.println("Number of likes for user: " + userLikes.size());
        assertEquals(1, userLikes.size());
        assertEquals(testExercise.getId(), userLikes.get(0).getExerciseId());

        // Check exercise's likes list
        var exerciseLikes = exerciseLikeService.getByExercise(testExercise.getId());
        System.out.println("Number of likes for exercise: " + exerciseLikes.size());
        assertEquals(1, exerciseLikes.size());
        assertEquals(testUser.getId(), exerciseLikes.get(0).getUserId());
    }

    @Test
    @Order(2)
    @DisplayName("Delete Like Test")
    void delete() {
        System.out.println("--- Delete Like Test ---");
        
        // First add like
        exerciseLikeService.add(testLikeDTO);
        System.out.println("Like added");

        // Check state before deletion
        boolean beforeDelete = exerciseLikeService.isLiked(testUser.getId(), testExercise.getId());
        System.out.println("State before deletion: " + beforeDelete);
        assertTrue(beforeDelete);

        // Delete like
        exerciseLikeService.delete(testUser.getId(), testExercise.getId());
        System.out.println("Like deleted");

        // Check state after deletion
        boolean afterDelete = exerciseLikeService.isLiked(testUser.getId(), testExercise.getId());
        System.out.println("State after deletion: " + afterDelete);
        assertFalse(afterDelete);

        // Check user's likes list
        var userLikes = exerciseLikeService.getByUser(testUser.getId());
        System.out.println("Number of likes for user after deletion: " + userLikes.size());
        assertTrue(userLikes.isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("Get Likes by User Test")
    void getByUser() {
        System.out.println("--- Get Likes by User Test ---");
        
        // Add like
        exerciseLikeService.add(testLikeDTO);
        System.out.println("Like added");

        // Get user's likes
        var userLikes = exerciseLikeService.getByUser(testUser.getId());
        System.out.println("Number of likes retrieved: " + userLikes.size());
        assertFalse(userLikes.isEmpty());
        assertEquals(1, userLikes.size());

        // Check like details
        var like = userLikes.get(0);
        System.out.println("Like details:");
        System.out.println("- User ID: " + like.getUserId());
        System.out.println("- Exercise ID: " + like.getExerciseId());
        assertEquals(testUser.getId(), like.getUserId());
        assertEquals(testExercise.getId(), like.getExerciseId());
    }

    @Test
    @Order(4)
    @DisplayName("Get Likes by Exercise Test")
    void getByExercise() {
        System.out.println("--- Get Likes by Exercise Test ---");
        
        // Add like
        exerciseLikeService.add(testLikeDTO);
        System.out.println("Like added");

        // Get exercise's likes
        var exerciseLikes = exerciseLikeService.getByExercise(testExercise.getId());
        System.out.println("Number of likes retrieved: " + exerciseLikes.size());
        assertFalse(exerciseLikes.isEmpty());
        assertEquals(1, exerciseLikes.size());

        // Check like details
        var like = exerciseLikes.get(0);
        System.out.println("Like details:");
        System.out.println("- User ID: " + like.getUserId());
        System.out.println("- Exercise ID: " + like.getExerciseId());
        assertEquals(testUser.getId(), like.getUserId());
        assertEquals(testExercise.getId(), like.getExerciseId());
    }

    @Test
    @Order(5)
    @DisplayName("Check Like Status Test")
    void isLiked() {
        System.out.println("--- Check Like Status Test ---");
        
        // Check state before adding
        boolean beforeAdd = exerciseLikeService.isLiked(testUser.getId(), testExercise.getId());
        System.out.println("State before adding: " + beforeAdd);
        assertFalse(beforeAdd);

        // Add like
        exerciseLikeService.add(testLikeDTO);
        System.out.println("Like added");

        // Check state after adding
        boolean afterAdd = exerciseLikeService.isLiked(testUser.getId(), testExercise.getId());
        System.out.println("State after adding: " + afterAdd);
        assertTrue(afterAdd);

        // Check non-existent user/exercise combination (expected exception)
        assertThrows(EntityNotFoundException.class, () -> {
            exerciseLikeService.isLiked(999, 999);
        }, "An exception should be thrown for non-existent user/exercise combinations");
        System.out.println("Exception for non-existent user/exercise combination confirmed");
    }

    @Test
    @Order(6)
    @DisplayName("Duplicate Like Prevention Test")
    void duplicateLikePrevention() {
        System.out.println("--- Duplicate Like Prevention Test ---");
        
        // First add like
        exerciseLikeService.add(testLikeDTO);
        System.out.println("First like added");

        // Check number of likes
        var userLikesBefore = exerciseLikeService.getByUser(testUser.getId());
        System.out.println("Number of likes after first addition: " + userLikesBefore.size());
        assertEquals(1, userLikesBefore.size());

        // Try duplicate like (expected exception)
        assertThrows(IllegalStateException.class, () -> {
            exerciseLikeService.add(testLikeDTO);
        }, "An exception should be thrown for duplicate likes");
        System.out.println("Exception for duplicate like confirmed");

        // Check number of likes (should still be 1)
        var userLikesAfter = exerciseLikeService.getByUser(testUser.getId());
        System.out.println("Number of likes after duplicate attempt: " + userLikesAfter.size());
        assertEquals(1, userLikesAfter.size());

        // Check like status
        boolean isLiked = exerciseLikeService.isLiked(testUser.getId(), testExercise.getId());
        System.out.println("Like status after duplicate attempt: " + isLiked);
        assertTrue(isLiked);
    }

    @AfterAll
    static void tearDown() {
        System.out.println("=== ExerciseLikeService Test Complete ===");
    }
}
