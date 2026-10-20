package com.skillmatch.controller;

import com.skillmatch.dto.*;
import com.skillmatch.entity.User;
import com.skillmatch.service.ApplicationService;
import com.skillmatch.service.AuthService;
import com.skillmatch.service.CompanyService;
import com.skillmatch.service.OpportunityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@PreAuthorize("hasRole('COMPANY')")
public class CompanyController {

    private final CompanyService companyService;
    private final AuthService authService;
    private final OpportunityService opportunityService;
    private final ApplicationService applicationService;

    public CompanyController(CompanyService companyService,
                             AuthService authService,
                             OpportunityService opportunityService,
                             ApplicationService applicationService) {
        this.companyService = companyService;
        this.authService = authService;
        this.opportunityService = opportunityService;
        this.applicationService = applicationService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CompanyResponse>> getMyCompanyProfile() {
        User user = authService.getCurrentUser();
        CompanyResponse profile = companyService.getProfileByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Company profile retrieved.", profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateMyCompanyProfile(@Valid @RequestBody CompanyUpdateRequest request) {
        User user = authService.getCurrentUser();
        CompanyResponse updated = companyService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Company profile updated successfully.", updated));
    }

    @GetMapping("/me/opportunities")
    public ResponseEntity<ApiResponse<List<OpportunityResponse>>> getMyOpportunities() {
        User user = authService.getCurrentUser();
        List<OpportunityResponse> opps = opportunityService.getCompanyOpportunities(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Company opportunities retrieved.", opps));
    }

    @GetMapping("/me/opportunities/{id}/applicants")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getOpportunityApplicants(@PathVariable("id") Long id) {
        User user = authService.getCurrentUser();
        List<ApplicationResponse> applicants = applicationService.getOpportunityApplicants(user.getId(), id, false);
        return ResponseEntity.ok(ApiResponse.ok("Applicants retrieved.", applicants));
    }

    @GetMapping("/me/applicants")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getAllCompanyApplicants() {
        User user = authService.getCurrentUser();
        List<ApplicationResponse> applicants = applicationService.getCompanyApplications(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("All applicants retrieved.", applicants));
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<ApiResponse<CompanyDashboardStats>> getDashboardStats() {
        User user = authService.getCurrentUser();
        CompanyDashboardStats stats = companyService.getDashboardStats(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Company dashboard stats retrieved.", stats));
    }
}
