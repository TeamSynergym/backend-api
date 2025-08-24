package org.synergym.backendapi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Exercises")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exercise extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_id")
    private int id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(length = 150)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "difficulty", length = 50)
    private String difficulty;

    @Column(name = "posture", length = 150)
    private String posture;

    @Column(name = "body_part", length = 150)
    private String bodyPart;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "url", length = 500)
    private String url;

    @Builder
    public Exercise(String name, String category, String description, String difficulty, String posture,
            String bodyPart, String thumbnailUrl, String url) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.difficulty = difficulty;
        this.posture = posture;
        this.bodyPart = bodyPart;
        this.thumbnailUrl = thumbnailUrl;
        this.url = url;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }

}