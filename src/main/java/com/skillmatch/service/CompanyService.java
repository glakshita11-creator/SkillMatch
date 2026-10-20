package com.skillmatch.service;

import com.skillmatch.dto.CompanyDashboardStats;
import com.skillmatch.dto.CompanyResponse;
import com.skillmatch.dto.CompanyUpdateRequest;
import com.skillmatch.entity.Application;
import com.skillmatch.entity.Company;
import com.skillmatch.entity.Opportunity;
import com.skillmatch.entity.User;
import com.skillmatch.enums.ApplicationStatus;
import com.skillmatch.enums.OpportunityStatus;
import com.skillmatch.exception.ResourceNotFoundException;
import com.skillmatch.repository.ApplicationRepository;
import com.skillmatch.repository.CompanyRepository;
import com.skillmatch.repository.OpportunityRepository;
import com.skillmatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final OpportunityRepository opportunityRepository;
    private final ApplicationRepository applicationRepository;

    public CompanyService(CompanyRepository companyRepository,
                          UserRepository userRepository,
                          OpportunityRepository opportunityRepository,
                          ApplicationRepository applicationRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
    }

    public Company getCompanyEntityByUserId(Long userId) {
        return companyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found for user ID: " + userId));
    }

    public CompanyResponse getProfileByUserId(Long userId) {
        Company company = getCompanyEntityByUserId(userId);
        return toResponse(company);
    }

    @Transactional
    public CompanyResponse updateProfile(Long userId, CompanyUpdateRequest request) {
        Company company = getCompanyEntityByUserId(userId);
        User user = company.getUser();

        if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
            company.setCompanyName(request.getCompanyName().trim());
        }
        if (request.getDescription() != null) {
            company.setDescription(request.getDescription().trim());
        }
        if (request.getWebsite() != null) {
            company.setWebsite(request.getWebsite().trim());
        }
        if (request.getContactEmail() != null) {
            company.setContactEmail(request.getContactEmail().trim());
        }
        if (request.getContactPhone() != null) {
            company.setContactPhone(request.getContactPhone().trim());
            user.setPhone(request.getContactPhone().trim());
            userRepository.save(user);
        }
        if (request.getLocation() != null) {
            company.setLocation(request.getLocation().trim());
        }
        if (request.getIndustry() != null) {
            company.setIndustry(request.getIndustry().trim());
        }

        company = companyRepository.save(company);
        return toResponse(company);
    }

    public CompanyDashboardStats getDashboardStats(Long userId) {
        Company company = getCompanyEntityByUserId(userId);
        List<Opportunity> opps = opportunityRepository.findByCompany(company);
        List<Application> apps = applicationRepository.findByOpportunityCompanyIdOrderByAppliedAtDesc(company.getId());

        CompanyDashboardStats stats = new CompanyDashboardStats();
        stats.setTotalOpportunities(opps.size());
        stats.setActiveOpportunities(opps.stream().filter(o -> o.getStatus() == OpportunityStatus.PUBLISHED).count());
        stats.setTotalApplicants(apps.size());
        stats.setUnderReviewCount(apps.stream().filter(a -> a.getStatus() == ApplicationStatus.UNDER_REVIEW).count());
        stats.setShortlistedCount(apps.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count());
        stats.setInterviewScheduledCount(apps.stream().filter(a -> a.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED).count());
        stats.setSelectedCount(apps.stream().filter(a -> a.getStatus() == ApplicationStatus.SELECTED).count());
        stats.setRejectedCount(apps.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count());

        return stats;
    }

    public CompanyResponse toResponse(Company company) {
        CompanyResponse resp = new CompanyResponse();
        resp.setId(company.getId());
        resp.setUserId(company.getUser().getId());
        resp.setEmail(company.getUser().getEmail());
        resp.setCompanyName(company.getCompanyName());
        resp.setDescription(company.getDescription());
        resp.setWebsite(company.getWebsite());
        resp.setContactEmail(company.getContactEmail());
        resp.setContactPhone(company.getContactPhone());
        resp.setLocation(company.getLocation());
        resp.setIndustry(company.getIndustry());
        resp.setUpdatedAt(company.getUpdatedAt());
        return resp;
    }
}
