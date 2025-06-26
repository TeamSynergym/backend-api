package org.synergym.backendapi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Exercises")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exercise extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_id")
    private int id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "difficulty", length = 20)
    private String difficulty;

    @Column(name = "posture", length = 50)
    private String posture;

    @Column(name = "body_part", length = 50)
    private String bodyPart;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @Builder
    public Exercise(String name, String category, String description, String difficulty, String posture, String bodyPart, String thumbnailUrl) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.difficulty = difficulty;
        this.posture = posture;
        this.bodyPart = bodyPart;
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }

//    public void updateName(String newName) {
//        this.name = newName;
//    }
//
//    public void updateCategory(String newCategory) {
//        this.category = newCategory;
//    }
//
//    public void updateDescription(String newDescription) {
//        this.description = newDescription;
//    }
//
//    public void updateDifficulty(String newDifficulty) {
//        this.difficulty = newDifficulty;
//    }
//
//    public void updatePosture(String newPosture) {
//        this.posture = newPosture;
//    }
//
//    public void updateBodyPart(String newBodyPart) {
//        this.bodyPart = newBodyPart;
//    }
//
//    public void updateThumbnailUrl(String newThumbnailUrl) {
//        this.thumbnailUrl = newThumbnailUrl;
//    }
}