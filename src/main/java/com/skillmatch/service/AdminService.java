package com.skillmatch.service;

import com.skillmatch.dto.*;
import com.skillmatch.entity.Application;
import com.skillmatch.entity.Company;
import com.skillmatch.entity.Opportunity;
import com.skillmatch.entity.User;
import com.skillmatch.enums.ApplicationStatus;
import com.skillmatch.enums.OpportunityStatus;
import com.skillmatch.enums.Role;
import com.skillmatch.exception.BadRequestException;
import com.skillmatch.exception.ResourceNotFoundException;
import com.skillmatch.repository.ApplicationRepository;
import com.skillmatch.repository.CompanyRepository;
import com.skillmatch.repository.OpportunityRepository;
import com.skillmatch.repository.StudentProfileRepository;
import com.skillmatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyRepository companyRepository;
    private final OpportunityRepository opportunityRepository;
    private final ApplicationRepository applicationRepository;
    private final AuthService authService;
    private final CompanyService companyService;
    private final OpportunityService opportunityService;
    private final ApplicationService applicationService;

    public AdminService(UserRepository userRepository,
                        StudentProfileRepository studentProfileRepository,
                        CompanyRepository companyRepository,
                        OpportunityRepository opportunityRepository,
                        ApplicationRepository applicationRepository,
                        AuthService authService,
                        CompanyService companyService,
                        OpportunityService opportunityService,
                        ApplicationService applicationService) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyRepository = companyRepository;
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
        this.authService = authService;
        this.companyService = companyService;
        this.opportunityService = opportunityService;
        this.applicationService = applicationService;
    }

    public AdminDashboardStats getDashboardStats() {
        AdminDashboardStats stats = new AdminDashboardStats();

        long totalStudents = userRepository.countByRole(Role.ROLE_STUDENT);
        long totalCompanies = userRepository.countByRole(Role.ROLE_COMPANY);
        long totalOpps = opportunityRepository.count();
        long activeOpps = opportunityRepository.countByStatus(OpportunityStatus.PUBLISHED);
        long totalApps = applicationRepository.count();
        long selected = applicationRepository.countByStatus(ApplicationStatus.SELECTED);
        long shortlisted = applicationRepository.countByStatus(ApplicationStatus.SHORTLISTED);
        long activeUsers = userRepository.countByActiveTrue();

        stats.setTotalStudents(totalStudents);
        stats.setTotalCompanies(totalCompanies);
        stats.setTotalOpportunities(totalOpps);
        stats.setActiveOpportunities(activeOpps);
        stats.setTotalApplications(totalApps);
        stats.setSelectedCount(selected);
        stats.setShortlistedCount(shortlisted);
        stats.setActiveUsersCount(activeUsers);

        double rate = totalStudents > 0 ? ((double) selected / totalStudents) * 100.0 : 0.0;
        stats.setPlacementRate(Math.round(rate * 10.0) / 10.0);

        Map<String, Long> statusMap = new HashMap<>();
        for (ApplicationStatus s : ApplicationStatus.values()) {
            statusMap.put(s.name(), applicationRepository.countByStatus(s));
        }
        stats.setApplicationsByStatus(statusMap);

        return stats;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse toggleUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Protect primary admin from deactivation
        if (user.getRole() == Role.ROLE_ADMIN && !active) {
            long adminCount = userRepository.countByRole(Role.ROLE_ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot deactivate the only active system administrator.");
            }
        }

        user.setActive(active);
        user = userRepository.save(user);
        return authService.toUserResponse(user);
    }

    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(companyService::toResponse)
                .collect(Collectors.toList());
    }

    public List<OpportunityResponse> getAllOpportunities() {
        return opportunityRepository.findAll().stream()
                .map(o -> opportunityService.toResponse(o, null))
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(applicationService::toResponse)
                .collect(Collectors.toList());
    }
}
