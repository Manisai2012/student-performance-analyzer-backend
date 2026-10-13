package com.example.factory.controller;

import com.example.factory.entity.StudentMark;
import com.example.factory.entity.User;
import com.example.factory.repository.StudentMarkRepository;
import com.example.factory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/marks")
@CrossOrigin(origins = "*")
public class StudentMarkController {

    @Autowired
    private StudentMarkRepository markRepository;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName()).orElse(null);
    }

    private boolean isAdmin(User user) {
        return user != null && ("ADMIN".equalsIgnoreCase(user.getRole()) || "ROLE_ADMIN".equalsIgnoreCase(user.getRole()));
    }

    @GetMapping
    public ResponseEntity<?> getAllMarks(Principal principal, @RequestParam(required = false) String studentId) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        List<StudentMark> marks;
        if (isAdmin(user)) {
            if (studentId != null && !studentId.trim().isEmpty()) {
                marks = markRepository.findByStudentIdOrderByExamDateDesc(studentId.trim());
            } else {
                marks = markRepository.findAllByOrderByExamDateDesc();
            }
        } else {
            // Student sees their own records (by username or studentId)
            marks = markRepository.findByUsernameOrderByExamDateDesc(user.getUsername());
            if (marks.isEmpty()) {
                // If marks were seeded under user's fullName or username
                marks = markRepository.findAllByOrderByExamDateDesc();
            }
        }

        return ResponseEntity.ok(marks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMarkById(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        Optional<StudentMark> markOpt = markRepository.findById(id);
        if (markOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Mark record not found"));
        }

        StudentMark mark = markOpt.get();
        if (!isAdmin(user) && !user.getUsername().equalsIgnoreCase(mark.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
        }

        return ResponseEntity.ok(mark);
    }

    @PostMapping
    public ResponseEntity<?> createMark(@RequestBody StudentMark mark, Principal principal) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        Map<String, String> validationErrors = validateMark(mark);
        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(validationErrors);
        }

        if (mark.getUsername() == null || mark.getUsername().trim().isEmpty()) {
            mark.setUsername(user.getUsername());
        }

        if (mark.getExamDate() == null) {
            mark.setExamDate(LocalDate.now());
        }

        StudentMark saved = markRepository.save(mark);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMark(@PathVariable Long id, @RequestBody StudentMark updated, Principal principal) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        Optional<StudentMark> markOpt = markRepository.findById(id);
        if (markOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Mark record not found"));
        }

        StudentMark existing = markOpt.get();
        if (!isAdmin(user) && !user.getUsername().equalsIgnoreCase(existing.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
        }

        Map<String, String> validationErrors = validateMark(updated);
        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(validationErrors);
        }

        existing.setStudentName(updated.getStudentName());
        existing.setStudentId(updated.getStudentId());
        existing.setBranch(updated.getBranch());
        existing.setSemester(updated.getSemester());
        existing.setSubject(updated.getSubject());
        existing.setAssessmentType(updated.getAssessmentType());
        existing.setObtainedMarks(updated.getObtainedMarks());
        existing.setMaxMarks(updated.getMaxMarks());
        existing.setExamDate(updated.getExamDate() != null ? updated.getExamDate() : existing.getExamDate());
        existing.setRemarks(updated.getRemarks());

        StudentMark saved = markRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMark(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        Optional<StudentMark> markOpt = markRepository.findById(id);
        if (markOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Mark record not found"));
        }

        StudentMark existing = markOpt.get();
        if (!isAdmin(user) && !user.getUsername().equalsIgnoreCase(existing.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
        }

        markRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Mark record deleted successfully", "id", id));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getPerformanceSummary(Principal principal, @RequestParam(required = false) String studentId) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        List<StudentMark> marks;
        if (isAdmin(user) && studentId != null && !studentId.trim().isEmpty()) {
            marks = markRepository.findByStudentIdOrderByExamDateDesc(studentId.trim());
        } else if (isAdmin(user)) {
            marks = markRepository.findAllByOrderByExamDateDesc();
        } else {
            marks = markRepository.findByUsernameOrderByExamDateDesc(user.getUsername());
            if (marks.isEmpty()) {
                marks = markRepository.findAllByOrderByExamDateDesc();
            }
        }

        Map<String, Object> summary = computePerformanceAnalysis(marks);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(Principal principal, @RequestParam(required = false) String studentId) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<StudentMark> marks;
        if (isAdmin(user) && studentId != null && !studentId.trim().isEmpty()) {
            marks = markRepository.findByStudentIdOrderByExamDateDesc(studentId.trim());
        } else if (isAdmin(user)) {
            marks = markRepository.findAllByOrderByExamDateDesc();
        } else {
            marks = markRepository.findByUsernameOrderByExamDateDesc(user.getUsername());
            if (marks.isEmpty()) {
                marks = markRepository.findAllByOrderByExamDateDesc();
            }
        }

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Student Name,Student ID,Branch,Semester,Subject,Assessment Type,Obtained Marks,Max Marks,Percentage (%),Date,Status,Remarks\n");

        for (StudentMark m : marks) {
            double pct = m.getPercentage() != null ? m.getPercentage() : 0.0;
            String status = pct >= 40.0 ? "PASS" : "FAIL";
            csv.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.1f,%.1f,%.2f,%s,%s,\"%s\"\n",
                    m.getId(),
                    escapeCsv(m.getStudentName()),
                    escapeCsv(m.getStudentId()),
                    escapeCsv(m.getBranch()),
                    escapeCsv(m.getSemester()),
                    escapeCsv(m.getSubject()),
                    escapeCsv(m.getAssessmentType()),
                    m.getObtainedMarks(),
                    m.getMaxMarks(),
                    pct,
                    m.getExamDate() != null ? m.getExamDate().toString() : "",
                    status,
                    escapeCsv(m.getRemarks() != null ? m.getRemarks() : "")
            ));
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "student_performance_report.csv");

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    private String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }

    private Map<String, String> validateMark(StudentMark mark) {
        Map<String, String> errors = new HashMap<>();
        if (mark.getStudentName() == null || mark.getStudentName().trim().isEmpty()) {
            errors.put("studentName", "Student name is required.");
        }
        if (mark.getStudentId() == null || mark.getStudentId().trim().isEmpty()) {
            errors.put("studentId", "Student ID is required.");
        }
        if (mark.getSubject() == null || mark.getSubject().trim().isEmpty()) {
            errors.put("subject", "Subject name is required.");
        }
        if (mark.getAssessmentType() == null || mark.getAssessmentType().trim().isEmpty()) {
            errors.put("assessmentType", "Assessment type is required.");
        }
        if (mark.getMaxMarks() == null || mark.getMaxMarks() <= 0) {
            errors.put("maxMarks", "Maximum marks must be greater than 0.");
        }
        if (mark.getObtainedMarks() == null || mark.getObtainedMarks() < 0) {
            errors.put("obtainedMarks", "Obtained marks cannot be negative.");
        } else if (mark.getMaxMarks() != null && mark.getObtainedMarks() > mark.getMaxMarks()) {
            errors.put("obtainedMarks", "Obtained marks cannot exceed maximum marks.");
        }
        return errors;
    }

    private Map<String, Object> computePerformanceAnalysis(List<StudentMark> marks) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (marks.isEmpty()) {
            result.put("totalAssessments", 0);
            result.put("totalSubjects", 0);
            result.put("totalObtainedMarks", 0.0);
            result.put("totalMaxMarks", 0.0);
            result.put("overallPercentage", 0.0);
            result.put("averageMarks", 0.0);
            result.put("passFailStatus", "N/A");
            result.put("strongestSubject", null);
            result.put("weakestSubject", null);
            result.put("subjectWisePerformance", Collections.emptyList());
            result.put("trendPerformance", Collections.emptyList());
            result.put("recommendations", Collections.emptyList());
            return result;
        }

        double totalObtained = 0.0;
        double totalMax = 0.0;
        Set<String> distinctSubjects = new HashSet<>();

        // Group by subject
        Map<String, List<StudentMark>> subjectMap = new HashMap<>();

        for (StudentMark m : marks) {
            double obt = m.getObtainedMarks() != null ? m.getObtainedMarks() : 0.0;
            double max = m.getMaxMarks() != null ? m.getMaxMarks() : 0.0;
            totalObtained += obt;
            totalMax += max;
            distinctSubjects.add(m.getSubject());
            subjectMap.computeIfAbsent(m.getSubject(), k -> new ArrayList<>()).add(m);
        }

        double overallPercentage = totalMax > 0 ? Math.round((totalObtained / totalMax) * 10000.0) / 100.0 : 0.0;
        double averageMarks = Math.round((totalObtained / marks.size()) * 100.0) / 100.0;
        String passFailStatus = overallPercentage >= 40.0 ? "PASS" : "FAIL";

        // Subject-wise details
        List<Map<String, Object>> subjectPerformances = new ArrayList<>();
        String strongestSubjectName = null;
        double maxSubjectPct = -1.0;
        String weakestSubjectName = null;
        double minSubjectPct = 101.0;

        for (Map.Entry<String, List<StudentMark>> entry : subjectMap.entrySet()) {
            String subject = entry.getKey();
            List<StudentMark> subMarks = entry.getValue();

            double subObt = subMarks.stream().mapToDouble(m -> m.getObtainedMarks() != null ? m.getObtainedMarks() : 0.0).sum();
            double subMax = subMarks.stream().mapToDouble(m -> m.getMaxMarks() != null ? m.getMaxMarks() : 0.0).sum();
            double subPct = subMax > 0 ? Math.round((subObt / subMax) * 10000.0) / 100.0 : 0.0;

            Map<String, Object> subData = new HashMap<>();
            subData.put("subject", subject);
            subData.put("obtainedMarks", subObt);
            subData.put("maxMarks", subMax);
            subData.put("percentage", subPct);
            subData.put("assessmentsCount", subMarks.size());
            subData.put("status", subPct >= 40.0 ? "PASS" : "FAIL");
            subjectPerformances.add(subData);

            if (subPct > maxSubjectPct) {
                maxSubjectPct = subPct;
                strongestSubjectName = subject;
            }
            if (subPct < minSubjectPct) {
                minSubjectPct = subPct;
                weakestSubjectName = subject;
            }
        }

        // Sort subject performances ascending by percentage to highlight weakest first
        subjectPerformances.sort(Comparator.comparingDouble(s -> (Double) s.get("percentage")));

        // Trend over time (sorted by examDate ascending)
        List<Map<String, Object>> trendPerformance = marks.stream()
                .filter(m -> m.getExamDate() != null)
                .sorted(Comparator.comparing(StudentMark::getExamDate))
                .map(m -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", m.getId());
                    item.put("date", m.getExamDate().toString());
                    item.put("subject", m.getSubject());
                    item.put("assessmentType", m.getAssessmentType());
                    item.put("percentage", m.getPercentage());
                    item.put("obtainedMarks", m.getObtainedMarks());
                    item.put("maxMarks", m.getMaxMarks());
                    return item;
                })
                .collect(Collectors.toList());

        // Performance Booster Recommendations based on rules
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (Map<String, Object> subData : subjectPerformances) {
            String subj = (String) subData.get("subject");
            double pct = (Double) subData.get("percentage");

            String level;
            String advice;
            String priority;
            String color;

            if (pct < 40.0) {
                level = "Critical Attention Needed (< 40%)";
                advice = "Focus on fundamentals and urgent revision. Book 1-on-1 tutoring, clarify foundational concepts, and solve basic numericals.";
                priority = "HIGH";
                color = "#ef4444";
            } else if (pct < 60.0) {
                level = "Needs Improvement (40% - 59%)";
                advice = "Practise questions and revise weak topics. Dedicate 45 minutes daily to problem solving and self-assessment quizzes.";
                priority = "MEDIUM-HIGH";
                color = "#f59e0b";
            } else if (pct < 80.0) {
                level = "Good Standing (60% - 79%)";
                advice = "Strengthen weak areas and solve previous exam questions. Target minor errors in complex problem types to push into distinction.";
                priority = "MEDIUM";
                color = "#3b82f6";
            } else {
                level = "Exemplary Performance (80%+)";
                advice = "Advanced practice and revision. Explore challenging Olympiad/Gate questions and peer-teach to maintain mastery.";
                priority = "LOW";
                color = "#10b981";
            }

            Map<String, Object> rec = new HashMap<>();
            rec.put("subject", subj);
            rec.put("percentage", pct);
            rec.put("level", level);
            rec.put("advice", advice);
            rec.put("priority", priority);
            rec.put("color", color);
            recommendations.add(rec);
        }

        // Student metadata from most recent mark
        StudentMark latest = marks.get(0);

        result.put("studentName", latest.getStudentName());
        result.put("studentId", latest.getStudentId());
        result.put("branch", latest.getBranch());
        result.put("semester", latest.getSemester());
        result.put("totalAssessments", marks.size());
        result.put("totalSubjects", distinctSubjects.size());
        result.put("totalObtainedMarks", Math.round(totalObtained * 100.0) / 100.0);
        result.put("totalMaxMarks", Math.round(totalMax * 100.0) / 100.0);
        result.put("overallPercentage", overallPercentage);
        result.put("averageMarks", averageMarks);
        result.put("passFailStatus", passFailStatus);
        result.put("strongestSubject", strongestSubjectName != null ? Map.of("name", strongestSubjectName, "percentage", maxSubjectPct) : null);
        result.put("weakestSubject", weakestSubjectName != null ? Map.of("name", weakestSubjectName, "percentage", minSubjectPct) : null);
        result.put("subjectWisePerformance", subjectPerformances);
        result.put("trendPerformance", trendPerformance);
        result.put("recommendations", recommendations);

        return result;
    }
}
