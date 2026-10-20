package com.skillmatch.controller;

import com.skillmatch.dto.ApiResponse;
import com.skillmatch.dto.ApplicationRequest;
import com.skillmatch.dto.ApplicationResponse;
import com.skillmatch.dto.ApplicationStatusUpdateRequest;
import com.skillmatch.entity.User;
import com.skillmatch.enums.Role;
import com.skillmatch.service.ApplicationService;
import com.skillmatch.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final AuthService authService;

    public ApplicationController(ApplicationService applicationService, AuthService authService) {
        this.applicationService = applicationService;
        this.authService = authService;
    }

    @PostMapping("/opportunities/{id}/applications")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> submitApplication(
            @PathVariable("id") Long opportunityId,
            @RequestBody(required = false) ApplicationRequest request) {
        User user = authService.getCurrentUser();
        ApplicationResponse created = applicationService.apply(user.getId(), opportunityId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Application submitted successfully.", created));
    }

    @GetMapping("/applications/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications() {
        User user = authService.getCurrentUser();
        List<ApplicationResponse> list = applicationService.getStudentApplications(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Applications retrieved.", list));
    }

    @GetMapping("/applications/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(@PathVariable("id") Long id) {
        User user = authService.getCurrentUser();
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        ApplicationResponse app = applicationService.getApplicationById(id, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Application details retrieved.", app));
    }

    @RequestMapping(value = "/applications/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        User user = authService.getCurrentUser();
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        ApplicationResponse updated = applicationService.updateApplicationStatus(user.getId(), id, request, isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Application status updated to " + updated.getStatusDisplayName() + ".", updated));
    }

    @PostMapping("/applications/{id}/withdraw")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> withdrawApplication(@PathVariable("id") Long id) {
        User user = authService.getCurrentUser();
        ApplicationResponse withdrawn = applicationService.withdrawApplication(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Application withdrawn successfully.", withdrawn));
    }
}
