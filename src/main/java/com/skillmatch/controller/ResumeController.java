package com.skillmatch.controller;

import com.skillmatch.entity.User;
import com.skillmatch.enums.Role;
import com.skillmatch.exception.ForbiddenException;
import com.skillmatch.service.ApplicationService;
import com.skillmatch.service.AuthService;
import com.skillmatch.service.ResumeStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeStorageService resumeStorageService;
    private final ApplicationService applicationService;
    private final AuthService authService;

    public ResumeController(ResumeStorageService resumeStorageService,
                            ApplicationService applicationService,
                            AuthService authService) {
        this.resumeStorageService = resumeStorageService;
        this.applicationService = applicationService;
        this.authService = authService;
    }

    @GetMapping("/{filename:.+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadResume(@PathVariable("filename") String filename) {
        User user = authService.getCurrentUser();
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;

        boolean authorized = applicationService.canAccessResume(filename, user.getId(), isAdmin);
        if (!authorized) {
            throw new ForbiddenException("You are not authorized to view or download this resume.");
        }

        Resource fileResource = resumeStorageService.loadAsResource(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(fileResource);
    }
}
