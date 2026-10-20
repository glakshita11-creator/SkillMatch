package com.skillmatch.controller;

import com.skillmatch.dto.*;
import com.skillmatch.service.AdminService;
import com.skillmatch.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardStats>> getDashboardStats() {
        AdminDashboardStats stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Admin statistics retrieved.", stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved.", users));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        UserResponse updated = adminService.toggleUserStatus(id, active);
        String msg = active ? "Account has been activated." : "Account has been deactivated.";
        return ResponseEntity.ok(ApiResponse.ok(msg, updated));
    }

    @PostMapping("/companies")
    public ResponseEntity<ApiResponse<UserResponse>> registerCompanyAccount(@Valid @RequestBody CompanyRegisterRequest request) {
        UserResponse response = authService.registerCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Company account registered successfully.", response));
    }

    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> getAllCompanies() {
        List<CompanyResponse> companies = adminService.getAllCompanies();
        return ResponseEntity.ok(ApiResponse.ok("Companies retrieved.", companies));
    }

    @GetMapping("/opportunities")
    public ResponseEntity<ApiResponse<List<OpportunityResponse>>> getAllOpportunities() {
        List<OpportunityResponse> opps = adminService.getAllOpportunities();
        return ResponseEntity.ok(ApiResponse.ok("Opportunities retrieved.", opps));
    }

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getAllApplications() {
        List<ApplicationResponse> apps = adminService.getAllApplications();
        return ResponseEntity.ok(ApiResponse.ok("Applications retrieved.", apps));
    }
}
