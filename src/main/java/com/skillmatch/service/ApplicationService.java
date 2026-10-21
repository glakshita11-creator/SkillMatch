package com.skillmatch.service;

import com.skillmatch.dto.ApplicationRequest;
import com.skillmatch.dto.ApplicationResponse;
import com.skillmatch.dto.ApplicationStatusUpdateRequest;
import com.skillmatch.dto.EligibilityCheckResponse;
import com.skillmatch.entity.Application;
import com.skillmatch.entity.Company;
import com.skillmatch.entity.Opportunity;
import com.skillmatch.entity.StudentProfile;
import com.skillmatch.entity.User;
import com.skillmatch.enums.ApplicationStatus;
import com.skillmatch.enums.Role;
import com.skillmatch.exception.BadRequestException;
import com.skillmatch.exception.DuplicateResourceException;
import com.skillmatch.exception.ForbiddenException;
import com.skillmatch.exception.IneligibleApplicationException;
import com.skillmatch.exception.ResourceNotFoundException;
import com.skillmatch.repository.ApplicationRepository;
import com.skillmatch.repository.OpportunityRepository;
import com.skillmatch.repository.StudentProfileRepository;
import com.skillmatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyService companyService;
    private final UserRepository userRepository;
    private final EligibilityService eligibilityService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              OpportunityRepository opportunityRepository,
                              StudentProfileRepository studentProfileRepository,
                              CompanyService companyService,
                              UserRepository userRepository,
                              EligibilityService eligibilityService) {
        this.applicationRepository = applicationRepository;
        this.opportunityRepository = opportunityRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyService = companyService;
        this.userRepository = userRepository;
        this.eligibilityService = eligibilityService;
    }

    public Application getApplicationEntity(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));
    }

    @Transactional
    public ApplicationResponse apply(Long studentUserId, Long opportunityId, ApplicationRequest request) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + studentUserId));

        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with ID: " + opportunityId));

        // 1. Check duplicate application
        if (applicationRepository.existsByStudentAndOpportunity(student, opportunity)) {
            throw new DuplicateResourceException("You have already applied for this opportunity.");
        }

        // 2. Eligibility verification
        EligibilityCheckResponse eligibility = eligibilityService.checkEligibility(student, opportunity);
        if (!eligibility.isEligible()) {
            throw new IneligibleApplicationException(
                    "Application cannot be submitted: You do not satisfy the eligibility requirements for this opportunity.",
                    eligibility.getReasons()
            );
        }

        // 3. Create and persist application
        Application app = new Application();
        app.setStudent(student);
        app.setOpportunity(opportunity);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setWithdrawn(false);
        if (request != null && request.getCoverNote() != null) {
            app.setCoverNote(request.getCoverNote().trim());
        }

        app = applicationRepository.save(app);
        return toResponse(app);
    }

    public List<ApplicationResponse> getStudentApplications(Long studentUserId) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        return applicationRepository.findByStudentOrderByAppliedAtDesc(student).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getOpportunityApplicants(Long companyUserId, Long opportunityId, boolean isAdmin) {
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with ID: " + opportunityId));

        if (!isAdmin) {
            Company company = companyService.getCompanyEntityByUserId(companyUserId);
            if (!opp.getCompany().getId().equals(company.getId())) {
                throw new ForbiddenException("You can only view applicants for your company's opportunities.");
            }
        }

        return applicationRepository.findByOpportunityOrderByAppliedAtDesc(opp).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getCompanyApplications(Long companyUserId) {
        Company company = companyService.getCompanyEntityByUserId(companyUserId);
        return applicationRepository.findByOpportunityCompanyIdOrderByAppliedAtDesc(company.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long companyUserId, Long applicationId, ApplicationStatusUpdateRequest request, boolean isAdmin) {
        Application app = getApplicationEntity(applicationId);

        if (!isAdmin) {
            Company company = companyService.getCompanyEntityByUserId(companyUserId);
            if (!app.getOpportunity().getCompany().getId().equals(company.getId())) {
                throw new ForbiddenException("You do not have permission to manage applications for other companies.");
            }

            // Verify status transition
            ApplicationStatus current = app.getStatus();
            List<ApplicationStatus> allowed = ApplicationStatus.getAllowedNextStatuses(current);
            if (!current.equals(request.getStatus()) && !allowed.contains(request.getStatus())) {
                throw new BadRequestException("Invalid status transition from " + current.getDisplayName() + " to " + request.getStatus().getDisplayName() + ".");
            }
        }

        app.setStatus(request.getStatus());
        if (request.getRecruiterNotes() != null) {
            app.setRecruiterNotes(request.getRecruiterNotes().trim());
        }
        if (request.getInterviewDate() != null) {
            app.setInterviewDate(request.getInterviewDate());
        }

        app = applicationRepository.save(app);
        return toResponse(app);
    }

    @Transactional
    public ApplicationResponse withdrawApplication(Long studentUserId, Long applicationId) {
        Application app = getApplicationEntity(applicationId);
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        if (!app.getStudent().getId().equals(student.getId())) {
            throw new ForbiddenException("You cannot withdraw another student's application.");
        }

        if (app.isWithdrawn()) {
            throw new BadRequestException("This application has already been withdrawn.");
        }

        if (!app.getStatus().canWithdraw()) {
            throw new BadRequestException("Applications cannot be withdrawn after a final decision (" + app.getStatus().getDisplayName() + ") has been reached.");
        }

        app.setWithdrawn(true);
        app.setRecruiterNotes((app.getRecruiterNotes() == null ? "" : app.getRecruiterNotes() + " | ") + "[Withdrawn by student]");
        app = applicationRepository.save(app);
        return toResponse(app);
    }

    public ApplicationResponse getApplicationById(Long applicationId, Long currentUserId, boolean isAdmin) {
        Application app = getApplicationEntity(applicationId);

        if (!isAdmin && currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found."));

            if (currentUser.getRole() == Role.ROLE_STUDENT) {
                if (!app.getStudent().getUser().getId().equals(currentUserId)) {
                    throw new ForbiddenException("You do not have permission to view this application.");
                }
            } else if (currentUser.getRole() == Role.ROLE_COMPANY) {
                Company company = companyService.getCompanyEntityByUserId(currentUserId);
                if (!app.getOpportunity().getCompany().getId().equals(company.getId())) {
                    throw new ForbiddenException("You do not have permission to view applicants of other companies.");
                }
            }
        }

        return toResponse(app);
    }

    public boolean canAccessResume(String filename, Long currentUserId, boolean isAdmin) {
        if (isAdmin) return true;
        if (currentUserId == null || filename == null) return false;

        User user = userRepository.findById(currentUserId).orElse(null);
        if (user == null) return false;

        if (user.getRole() == Role.ROLE_STUDENT) {
            StudentProfile profile = studentProfileRepository.findByUserId(currentUserId).orElse(null);
            return profile != null && filename.equals(profile.getResumeFileName());
        }

        if (user.getRole() == Role.ROLE_COMPANY) {
            Company company = companyService.getCompanyEntityByUserId(currentUserId);
            if (company == null) return false;
            // Company can access resume if student applied to an opportunity owned by this company
            List<Application> apps = applicationRepository.findByOpportunityCompanyIdOrderByAppliedAtDesc(company.getId());
            return apps.stream().anyMatch(a -> filename.equals(a.getStudent().getResumeFileName()));
        }

        return false;
    }

    public ApplicationResponse toResponse(Application app) {
        ApplicationResponse resp = new ApplicationResponse();
        resp.setId(app.getId());

        // Opportunity info
        Opportunity opp = app.getOpportunity();
        resp.setOpportunityId(opp.getId());
        resp.setOpportunityTitle(opp.getTitle());
        resp.setOpportunityType(opp.getOpportunityType().name());
        resp.setLocation(opp.getLocation());
        resp.setSalaryOrStipend(opp.getSalaryOrStipend());
        resp.setCompanyId(opp.getCompany().getId());
        resp.setCompanyName(opp.getCompany().getCompanyName());

        // Student info
        StudentProfile s = app.getStudent();
        resp.setStudentProfileId(s.getId());
        resp.setStudentUserId(s.getUser().getId());
        resp.setStudentName(s.getUser().getFullName());
        resp.setStudentEmail(s.getUser().getEmail());
        resp.setStudentPhone(s.getUser().getPhone());
        resp.setStudentDepartment(s.getDepartment());
        resp.setStudentDegree(s.getDegree());
        resp.setStudentGraduationYear(s.getGraduationYear());
        resp.setStudentCgpa(s.getCgpa());
        resp.setStudentSkills(s.getSkills());
        resp.setHasResume(s.getResumeFileName() != null && !s.getResumeFileName().isBlank());
        resp.setResumeOriginalName(s.getResumeOriginalName());
        resp.setResumeUrl(s.getResumeUrl());

        // Status & Workflow
        resp.setStatus(app.getStatus());
        resp.setCoverNote(app.getCoverNote());
        resp.setRecruiterNotes(app.getRecruiterNotes());
        resp.setInterviewDate(app.getInterviewDate());
        resp.setWithdrawn(app.isWithdrawn());
        resp.setCanWithdraw(!app.isWithdrawn() && app.getStatus().canWithdraw());
        resp.setAppliedAt(app.getAppliedAt());
        resp.setUpdatedAt(app.getUpdatedAt());

        return resp;
    }
}
