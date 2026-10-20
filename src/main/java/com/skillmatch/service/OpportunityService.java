package com.skillmatch.service;

import com.skillmatch.dto.EligibilityCheckResponse;
import com.skillmatch.dto.OpportunityRequest;
import com.skillmatch.dto.OpportunityResponse;
import com.skillmatch.entity.Application;
import com.skillmatch.entity.Company;
import com.skillmatch.entity.Opportunity;
import com.skillmatch.entity.StudentProfile;
import com.skillmatch.entity.User;
import com.skillmatch.enums.OpportunityStatus;
import com.skillmatch.enums.OpportunityType;
import com.skillmatch.enums.Role;
import com.skillmatch.exception.ForbiddenException;
import com.skillmatch.exception.ResourceNotFoundException;
import com.skillmatch.repository.ApplicationRepository;
import com.skillmatch.repository.OpportunityRepository;
import com.skillmatch.repository.StudentProfileRepository;
import com.skillmatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final CompanyService companyService;
    private final ApplicationRepository applicationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final EligibilityService eligibilityService;

    public OpportunityService(OpportunityRepository opportunityRepository,
                              CompanyService companyService,
                              ApplicationRepository applicationRepository,
                              StudentProfileRepository studentProfileRepository,
                              UserRepository userRepository,
                              EligibilityService eligibilityService) {
        this.opportunityRepository = opportunityRepository;
        this.companyService = companyService;
        this.applicationRepository = applicationRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.eligibilityService = eligibilityService;
    }

    public Opportunity getOpportunityEntity(Long opportunityId) {
        return opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with ID: " + opportunityId));
    }

    @Transactional
    public OpportunityResponse createOpportunity(Long companyUserId, OpportunityRequest request) {
        Company company = companyService.getCompanyEntityByUserId(companyUserId);

        Opportunity opp = new Opportunity();
        opp.setCompany(company);
        mapRequestToEntity(request, opp);
        opp = opportunityRepository.save(opp);

        return toResponse(opp, null);
    }

    @Transactional
    public OpportunityResponse updateOpportunity(Long companyUserId, Long opportunityId, OpportunityRequest request, boolean isAdmin) {
        Opportunity opp = getOpportunityEntity(opportunityId);

        if (!isAdmin) {
            Company company = companyService.getCompanyEntityByUserId(companyUserId);
            if (!opp.getCompany().getId().equals(company.getId())) {
                throw new ForbiddenException("You can only modify opportunities created by your company.");
            }
        }

        mapRequestToEntity(request, opp);
        opp = opportunityRepository.save(opp);
        return toResponse(opp, null);
    }

    @Transactional
    public OpportunityResponse publishOpportunity(Long companyUserId, Long opportunityId, boolean isAdmin) {
        Opportunity opp = getOpportunityEntity(opportunityId);
        verifyOwnership(opp, companyUserId, isAdmin);

        opp.setStatus(OpportunityStatus.PUBLISHED);
        opp = opportunityRepository.save(opp);
        return toResponse(opp, null);
    }

    @Transactional
    public OpportunityResponse closeOpportunity(Long companyUserId, Long opportunityId, boolean isAdmin) {
        Opportunity opp = getOpportunityEntity(opportunityId);
        verifyOwnership(opp, companyUserId, isAdmin);

        opp.setStatus(OpportunityStatus.CLOSED);
        opp = opportunityRepository.save(opp);
        return toResponse(opp, null);
    }

    @Transactional
    public void deleteOpportunity(Long companyUserId, Long opportunityId, boolean isAdmin) {
        Opportunity opp = getOpportunityEntity(opportunityId);
        verifyOwnership(opp, companyUserId, isAdmin);
        opportunityRepository.delete(opp);
    }

    public OpportunityResponse getOpportunityById(Long opportunityId, Long currentUserId) {
        Opportunity opp = getOpportunityEntity(opportunityId);
        StudentProfile student = resolveStudent(currentUserId);
        return toResponse(opp, student);
    }

    public List<OpportunityResponse> getCompanyOpportunities(Long companyUserId) {
        Company company = companyService.getCompanyEntityByUserId(companyUserId);
        List<Opportunity> opps = opportunityRepository.findByCompany(company);
        return opps.stream()
                .sorted(Comparator.comparing(Opportunity::getCreatedAt).reversed())
                .map(o -> toResponse(o, null))
                .collect(Collectors.toList());
    }

    public List<OpportunityResponse> searchOpportunities(String query,
                                                        OpportunityType type,
                                                        String department,
                                                        String location,
                                                        Integer graduationYear,
                                                        BigDecimal minCgpa,
                                                        String skill,
                                                        String sort,
                                                        Long currentUserId) {
        StudentProfile student = resolveStudent(currentUserId);
        List<Opportunity> all = opportunityRepository.findAll();

        return all.stream()
                .filter(o -> {
                    // Filter: Only published unless admin
                    if (o.getStatus() != OpportunityStatus.PUBLISHED) {
                        return false;
                    }
                    // Filter: text query (title, company, description, location, requiredSkills)
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase().trim();
                        boolean matchesText = o.getTitle().toLowerCase().contains(q)
                                || o.getCompany().getCompanyName().toLowerCase().contains(q)
                                || (o.getLocation() != null && o.getLocation().toLowerCase().contains(q))
                                || (o.getRequiredSkills() != null && o.getRequiredSkills().toLowerCase().contains(q))
                                || (o.getDescription() != null && o.getDescription().toLowerCase().contains(q));
                        if (!matchesText) return false;
                    }
                    // Filter: opportunity type
                    if (type != null && o.getOpportunityType() != type) {
                        return false;
                    }
                    // Filter: location
                    if (location != null && !location.isBlank()) {
                        if (o.getLocation() == null || !o.getLocation().toLowerCase().contains(location.toLowerCase().trim())) {
                            return false;
                        }
                    }
                    // Filter: department
                    if (department != null && !department.isBlank()) {
                        if (o.getEligibleDepartments() != null && !o.getEligibleDepartments().isBlank()) {
                            if (!o.getEligibleDepartments().toLowerCase().contains(department.toLowerCase().trim())) {
                                return false;
                            }
                        }
                    }
                    // Filter: graduation year
                    if (graduationYear != null) {
                        if (o.getEligibleGraduationYears() != null && !o.getEligibleGraduationYears().isBlank()) {
                            if (!o.getEligibleGraduationYears().contains(String.valueOf(graduationYear))) {
                                return false;
                            }
                        }
                    }
                    // Filter: CGPA eligibility
                    if (minCgpa != null) {
                        if (o.getMinCgpa() != null && o.getMinCgpa().compareTo(minCgpa) > 0) {
                            return false;
                        }
                    }
                    // Filter: skill
                    if (skill != null && !skill.isBlank()) {
                        if (o.getRequiredSkills() == null || !o.getRequiredSkills().toLowerCase().contains(skill.toLowerCase().trim())) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    if ("deadline".equalsIgnoreCase(sort)) {
                        return a.getDeadline().compareTo(b.getDeadline());
                    } else if ("company".equalsIgnoreCase(sort)) {
                        return a.getCompany().getCompanyName().compareToIgnoreCase(b.getCompany().getCompanyName());
                    } else {
                        // Default: newest created first
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                })
                .map(o -> toResponse(o, student))
                .collect(Collectors.toList());
    }

    private void verifyOwnership(Opportunity opp, Long companyUserId, boolean isAdmin) {
        if (isAdmin) return;
        Company company = companyService.getCompanyEntityByUserId(companyUserId);
        if (!opp.getCompany().getId().equals(company.getId())) {
            throw new ForbiddenException("You do not own this opportunity.");
        }
    }

    private StudentProfile resolveStudent(Long currentUserId) {
        if (currentUserId == null) return null;
        Optional<User> userOpt = userRepository.findById(currentUserId);
        if (userOpt.isPresent() && userOpt.get().getRole() == Role.ROLE_STUDENT) {
            return studentProfileRepository.findByUserId(currentUserId).orElse(null);
        }
        return null;
    }

    private void mapRequestToEntity(OpportunityRequest request, Opportunity opp) {
        opp.setTitle(request.getTitle().trim());
        opp.setDescription(request.getDescription().trim());
        opp.setOpportunityType(request.getOpportunityType());
        if (request.getWorkplaceType() != null) {
            opp.setWorkplaceType(request.getWorkplaceType());
        }
        opp.setLocation(request.getLocation().trim());
        opp.setRequiredSkills(request.getRequiredSkills());
        opp.setMinCgpa(request.getMinCgpa() != null ? request.getMinCgpa() : BigDecimal.ZERO);
        opp.setEligibleDepartments(request.getEligibleDepartments());
        opp.setEligibleGraduationYears(request.getEligibleGraduationYears());
        opp.setSalaryOrStipend(request.getSalaryOrStipend());
        opp.setOpenings(request.getOpenings() != null ? request.getOpenings() : 1);
        opp.setDeadline(request.getDeadline());
        if (request.getStatus() != null) {
            opp.setStatus(request.getStatus());
        }
    }

    public OpportunityResponse toResponse(Opportunity opp, StudentProfile student) {
        OpportunityResponse resp = new OpportunityResponse();
        resp.setId(opp.getId());
        resp.setCompanyId(opp.getCompany().getId());
        resp.setCompanyName(opp.getCompany().getCompanyName());
        resp.setCompanyLocation(opp.getCompany().getLocation());
        resp.setCompanyWebsite(opp.getCompany().getWebsite());
        resp.setTitle(opp.getTitle());
        resp.setDescription(opp.getDescription());
        resp.setOpportunityType(opp.getOpportunityType());
        resp.setWorkplaceType(opp.getWorkplaceType());
        resp.setLocation(opp.getLocation());
        resp.setRequiredSkills(opp.getRequiredSkills());
        resp.setMinCgpa(opp.getMinCgpa());
        resp.setEligibleDepartments(opp.getEligibleDepartments());
        resp.setEligibleGraduationYears(opp.getEligibleGraduationYears());
        resp.setSalaryOrStipend(opp.getSalaryOrStipend());
        resp.setOpenings(opp.getOpenings());
        resp.setDeadline(opp.getDeadline());
        resp.setStatus(opp.getStatus());
        resp.setCreatedAt(opp.getCreatedAt());

        long count = applicationRepository.findByOpportunityOrderByAppliedAtDesc(opp).size();
        resp.setApplicationCount(count);

        if (student != null) {
            EligibilityCheckResponse elig = eligibilityService.checkEligibility(student, opp);
            resp.setEligibleForCurrentUser(elig.isEligible());
            resp.setEligibilityReasons(elig.getReasons());

            Optional<Application> appOpt = applicationRepository.findByStudentAndOpportunity(student, opp);
            if (appOpt.isPresent()) {
                resp.setAlreadyApplied(true);
                resp.setUserApplicationId(appOpt.get().getId());
            } else {
                resp.setAlreadyApplied(false);
            }
        } else {
            resp.setEligibleForCurrentUser(true);
        }

        return resp;
    }
}
