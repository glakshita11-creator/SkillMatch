package com.skillmatch.controller;

import com.skillmatch.dto.ApiResponse;
import com.skillmatch.dto.EligibilityCheckResponse;
import com.skillmatch.dto.OpportunityRequest;
import com.skillmatch.dto.OpportunityResponse;
import com.skillmatch.entity.Opportunity;
import com.skillmatch.entity.StudentProfile;
import com.skillmatch.entity.User;
import com.skillmatch.enums.OpportunityType;
import com.skillmatch.enums.Role;
import com.skillmatch.service.AuthService;
import com.skillmatch.service.EligibilityService;
import com.skillmatch.service.OpportunityService;
import com.skillmatch.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final AuthService authService;
    private final StudentService studentService;
    private final EligibilityService eligibilityService;

    public OpportunityController(OpportunityService opportunityService,
                                 AuthService authService,
                                 StudentService studentService,
                                 EligibilityService eligibilityService) {
        this.opportunityService = opportunityService;
        this.authService = authService;
        this.studentService = studentService;
        this.eligibilityService = eligibilityService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OpportunityResponse>>> getOpportunities(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "type", required = false) OpportunityType type,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "graduationYear", required = false) Integer graduationYear,
            @RequestParam(value = "minCgpa", required = false) BigDecimal minCgpa,
            @RequestParam(value = "skill", required = false) String skill,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort) {

        Long currentUserId = resolveCurrentUserId();
        List<OpportunityResponse> results = opportunityService.searchOpportunities(
                query, type, department, location, graduationYear, minCgpa, skill, sort, currentUserId
        );
        return ResponseEntity.ok(ApiResponse.ok("Opportunities retrieved.", results));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OpportunityResponse>> getOpportunityById(@PathVariable("id") Long id) {
        Long currentUserId = resolveCurrentUserId();
        OpportunityResponse opp = opportunityService.getOpportunityById(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok("Opportunity details retrieved.", opp));
    }

    @GetMapping("/{id}/eligibility")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EligibilityCheckResponse>> checkEligibility(@PathVariable("id") Long id) {
        User user = authService.getCurrentUser();
        StudentProfile student = studentService.getStudentProfileEntityByUserId(user.getId());
        Opportunity opportunity = opportunityService.getOpportunityEntity(id);

        EligibilityCheckResponse result = eligibilityService.checkEligibility(student, opportunity);
        return ResponseEntity.ok(ApiResponse.ok("Eligibility evaluation complete.", result));
    }

    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApiResponse<OpportunityResponse>> createOpportunity(@Valid @RequestBody OpportunityRequest request) {
        User user = authService.getCurrentUser();
        OpportunityResponse created = opportunityService.createOpportunity(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Opportunity created successfully.", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<ApiResponse<OpportunityResponse>> updateOpportunity(
            @PathVariable("id") Long id,
            @Valid @RequestBody OpportunityRequest request) {
        User user = authService.getCurrentUser();
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        OpportunityResponse updated = opportunityService.updateOpportunity(user.getId(), id, request, isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Opportunity updated successfully.", updated));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<ApiResponse<OpportunityResponse>> publishOpportunity(@PathVariable("id") Long id) {
        User user = authService.getCurrentUser();
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        OpportunityResponse updated = opportunityService.publishOpportunity(user.getId(), id, isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Opportunity published successfully.", updated));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<ApiResponse<OpportunityResponse>> closeOpportunity(@PathVariable("id") Long id) {
        User user = authService.getCurrentUser();
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        OpportunityResponse updated = opportunityService.closeOpportunity(user.getId(), id, isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Opportunity closed successfully.", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOpportunity(@PathVariable("id") Long id) {
        User user = authService.getCurrentUser();
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        opportunityService.deleteOpportunity(user.getId(), id, isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Opportunity deleted successfully."));
    }

    private Long resolveCurrentUserId() {
        try {
            return authService.getCurrentUser().getId();
        } catch (Exception e) {
            return null;
        }
    }
}
