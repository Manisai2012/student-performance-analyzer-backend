package com.example.factory;

import com.example.factory.entity.*;
import com.example.factory.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(MachineRepository machineRepo,
                                   WorkerRepository workerRepo,
                                   ProductRepository productRepo,
                                   OrderRepository orderRepo,
                                   UserRepository userRepo,
                                   PasswordEncoder passwordEncoder,
                                   StudentMarkRepository markRepo) {
        return args -> {
            // Seed Users
            if (userRepo.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@university.edu");
                admin.setFullName("Academic Administrator");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                admin.setStatus("ACTIVE");
                userRepo.save(admin);
            }

            if (userRepo.findByUsername("student").isEmpty()) {
                User student = new User();
                student.setUsername("student");
                student.setEmail("alex.johnson@university.edu");
                student.setFullName("Alex Johnson");
                student.setPassword(passwordEncoder.encode("student123"));
                student.setRole("STUDENT");
                student.setStatus("ACTIVE");
                userRepo.save(student);
            }

            // Seed Student Marks
            if (markRepo.count() == 0) {
                createMark(markRepo, "Alex Johnson", "STU-1024", "Computer Science", "Semester 5",
                        "Data Structures & Algorithms", "Midterm Exam", 88.0, 100.0, LocalDate.of(2026, 8, 15), "student", "Strong problem solving in trees");

                createMark(markRepo, "Alex Johnson", "STU-1024", "Computer Science", "Semester 5",
                        "Database Management Systems", "Midterm Exam", 74.0, 100.0, LocalDate.of(2026, 8, 20), "student", "Solid SQL queries, need normalization revision");

                createMark(markRepo, "Alex Johnson", "STU-1024", "Computer Science", "Semester 5",
                        "Computer Networks", "Quiz 1", 13.0, 20.0, LocalDate.of(2026, 9, 2), "student", "Good grasp of OSI layers");

                createMark(markRepo, "Alex Johnson", "STU-1024", "Computer Science", "Semester 5",
                        "Operating Systems", "Midterm Exam", 34.0, 100.0, LocalDate.of(2026, 9, 10), "student", "Weak in semaphore synchronization and deadlock analysis");

                createMark(markRepo, "Alex Johnson", "STU-1024", "Computer Science", "Semester 5",
                        "Discrete Mathematics", "Unit Test 1", 46.0, 50.0, LocalDate.of(2026, 9, 18), "student", "Excellent proofs and graph theory");

                createMark(markRepo, "Alex Johnson", "STU-1024", "Computer Science", "Semester 5",
                        "Software Engineering", "Project Review", 42.0, 50.0, LocalDate.of(2026, 9, 25), "student", "Great architectural diagrams and documentation");

                createMark(markRepo, "Alex Johnson", "STU-1024", "Computer Science", "Semester 5",
                        "Operating Systems", "Lab Assignment", 11.0, 30.0, LocalDate.of(2026, 10, 5), "student", "Struggled with thread concurrency task");
            }

            // Preserve existing legacy demo seeds for backwards compatibility
            if (machineRepo.count() == 0) {
                Machine m1 = new Machine();
                m1.setName("Lab Computing Server A");
                m1.setType("Server");
                m1.setDepartment("Academic Labs");
                m1.setLocation("Zone 1");
                m1.setStatus("Running");
                m1.setTemperature(45.5);
                m1.setOperatingHours(1200);
                m1.setProductionCount(5000);
                machineRepo.save(m1);
            }
        };
    }

    private void createMark(StudentMarkRepository repo, String name, String studentId, String branch, String sem,
                            String subject, String assessmentType, Double obt, Double max, LocalDate date, String username, String remarks) {
        StudentMark m = new StudentMark();
        m.setStudentName(name);
        m.setStudentId(studentId);
        m.setBranch(branch);
        m.setSemester(sem);
        m.setSubject(subject);
        m.setAssessmentType(assessmentType);
        m.setObtainedMarks(obt);
        m.setMaxMarks(max);
        m.setExamDate(date);
        m.setUsername(username);
        m.setRemarks(remarks);
        repo.save(m);
    }
}
