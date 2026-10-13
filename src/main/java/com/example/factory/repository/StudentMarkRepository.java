package com.example.factory.repository;

import com.example.factory.entity.StudentMark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentMarkRepository extends JpaRepository<StudentMark, Long> {
    List<StudentMark> findByUsernameOrderByExamDateDesc(String username);
    List<StudentMark> findByStudentIdOrderByExamDateDesc(String studentId);
    List<StudentMark> findAllByOrderByExamDateDesc();
}
