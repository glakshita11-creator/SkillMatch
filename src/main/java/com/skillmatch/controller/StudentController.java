package com.skillmatch.controller;

import com.skillmatch.dto.*;
import com.skillmatch.entity.User;
import com.skillmatch.service.ApplicationService;
import com.skillmatch.service.AuthService;
import com.skillmatch.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final StudentService studentService;
    private final AuthService authService;
    private final ApplicationService applicationService;

    public StudentController(StudentService studentService,
                             AuthService authService,
                             ApplicationService applicationService) {
        this.studentService = studentService;
        this.authService = authService;
        this.applicationService = applicationService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getMyProfile() {
        User user = authService.getCurrentUser();
        StudentProfileResponse profile = studentService.getProfileByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved successfully.", profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> updateMyProfile(@Valid @RequestBody StudentProfileUpdateRequest request) {
        User user = authService.getCurrentUser();
        StudentProfileResponse updated = studentService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully.", updated));
    }

    @PostMapping("/me/resume")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> uploadResume(@RequestParam("file") MultipartFile file) {
        User user = authService.getCurrentUser();
        StudentProfileResponse updated = studentService.uploadResume(user.getId(), file);
        return ResponseEntity.ok(ApiResponse.ok("Resume uploaded successfully.", updated));
    }

    @GetMapping("/me/applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications() {
        User user = authService.getCurrentUser();
        List<ApplicationResponse> applications = applicationService.getStudentApplications(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Applications retrieved successfully.", applications));
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<ApiResponse<StudentDashboardStats>> getDashboardStats() {
        User user = authService.getCurrentUser();
        StudentDashboardStats stats = studentService.getDashboardStats(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Dashboard statistics retrieved.", stats));
    }
}
