package com.example.factory.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "student_marks")
public class StudentMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false)
    private String studentId;

    private String branch;

    private String semester;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String assessmentType;

    @Column(nullable = false)
    private Double obtainedMarks;

    @Column(nullable = false)
    private Double maxMarks;

    private Double percentage;

    private LocalDate examDate;

    private String username; // Creator or owner student username

    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (maxMarks != null && maxMarks > 0 && obtainedMarks != null) {
            this.percentage = Math.round((obtainedMarks / maxMarks) * 10000.0) / 100.0;
        } else {
            this.percentage = 0.0;
        }
    }
}
