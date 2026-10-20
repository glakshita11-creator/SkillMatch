package com.skillmatch.service;

import com.skillmatch.dto.StudentDashboardStats;
import com.skillmatch.dto.StudentProfileResponse;
import com.skillmatch.dto.StudentProfileUpdateRequest;
import com.skillmatch.entity.Application;
import com.skillmatch.entity.StudentProfile;
import com.skillmatch.entity.User;
import com.skillmatch.enums.ApplicationStatus;
import com.skillmatch.enums.OpportunityStatus;
import com.skillmatch.exception.ResourceNotFoundException;
import com.skillmatch.repository.ApplicationRepository;
import com.skillmatch.repository.OpportunityRepository;
import com.skillmatch.repository.StudentProfileRepository;
import com.skillmatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final ResumeStorageService resumeStorageService;

    public StudentService(StudentProfileRepository studentProfileRepository,
                          UserRepository userRepository,
                          ApplicationRepository applicationRepository,
                          OpportunityRepository opportunityRepository,
                          ResumeStorageService resumeStorageService) {
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.opportunityRepository = opportunityRepository;
        this.resumeStorageService = resumeStorageService;
    }

    public StudentProfile getStudentProfileEntityByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + userId));
    }

    public StudentProfileResponse getProfileByUserId(Long userId) {
        StudentProfile profile = getStudentProfileEntityByUserId(userId);
        return toResponse(profile);
    }

    @Transactional
    public StudentProfileResponse updateProfile(Long userId, StudentProfileUpdateRequest request) {
        StudentProfile profile = getStudentProfileEntityByUserId(userId);
        User user = profile.getUser();

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        userRepository.save(user);

        profile.setDepartment(request.getDepartment());
        profile.setDegree(request.getDegree());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setCgpa(request.getCgpa());
        profile.setSkills(request.getSkills());
        if (request.getResumeUrl() != null) {
            profile.setResumeUrl(request.getResumeUrl().trim());
        }
        profile.setBio(request.getBio());

        profile = studentProfileRepository.save(profile);
        return toResponse(profile);
    }

    @Transactional
    public StudentProfileResponse uploadResume(Long userId, MultipartFile file) {
        StudentProfile profile = getStudentProfileEntityByUserId(userId);

        // Delete old file if present
        if (profile.getResumeFileName() != null) {
            resumeStorageService.deleteFile(profile.getResumeFileName());
        }

        String storedFilename = resumeStorageService.store(file);
        profile.setResumeFileName(storedFilename);
        profile.setResumeOriginalName(file.getOriginalFilename());
        profile.setResumeContentType(file.getContentType());

        profile = studentProfileRepository.save(profile);
        return toResponse(profile);
    }

    public StudentDashboardStats getDashboardStats(Long userId) {
        StudentProfile profile = getStudentProfileEntityByUserId(userId);
        List<Application> apps = applicationRepository.findByStudentOrderByAppliedAtDesc(profile);

        StudentDashboardStats stats = new StudentDashboardStats();
        stats.setTotalApplications(apps.size());
        stats.setUnderReviewCount(apps.stream().filter(a -> a.getStatus() == ApplicationStatus.UNDER_REVIEW).count());
        stats.setShortlistedCount(apps.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count());
        stats.setInterviewScheduledCount(apps.stream().filter(a -> a.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED).count());
        stats.setSelectedCount(apps.stream().filter(a -> a.getStatus() == ApplicationStatus.SELECTED).count());
        stats.setRejectedCount(apps.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count());

        stats.setActiveOpportunitiesCount(opportunityRepository.countByStatus(OpportunityStatus.PUBLISHED));

        boolean complete = profile.getDepartment() != null && !profile.getDepartment().isBlank()
                && profile.getGraduationYear() != null
                && profile.getCgpa() != null
                && (profile.getResumeFileName() != null || (profile.getResumeUrl() != null && !profile.getResumeUrl().isBlank()));
        stats.setProfileComplete(complete);

        return stats;
    }

    public StudentProfileResponse toResponse(StudentProfile profile) {
        StudentProfileResponse resp = new StudentProfileResponse();
        resp.setId(profile.getId());
        resp.setUserId(profile.getUser().getId());
        resp.setEmail(profile.getUser().getEmail());
        resp.setFullName(profile.getUser().getFullName());
        resp.setPhone(profile.getUser().getPhone());
        resp.setDepartment(profile.getDepartment());
        resp.setDegree(profile.getDegree());
        resp.setGraduationYear(profile.getGraduationYear());
        resp.setCgpa(profile.getCgpa());
        resp.setSkills(profile.getSkills());
        resp.setResumeFileName(profile.getResumeFileName());
        resp.setResumeOriginalName(profile.getResumeOriginalName());
        resp.setResumeUrl(profile.getResumeUrl());
        resp.setBio(profile.getBio());
        resp.setHasResume(profile.getResumeFileName() != null && !profile.getResumeFileName().isBlank());
        resp.setUpdatedAt(profile.getUpdatedAt());
        return resp;
    }
}
