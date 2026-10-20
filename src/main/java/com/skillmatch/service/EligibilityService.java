package com.skillmatch.service;

import com.skillmatch.dto.EligibilityCheckResponse;
import com.skillmatch.entity.Opportunity;
import com.skillmatch.entity.StudentProfile;
import com.skillmatch.enums.OpportunityStatus;
import com.skillmatch.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EligibilityService {

    private final ApplicationRepository applicationRepository;

    public EligibilityService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public EligibilityCheckResponse checkEligibility(StudentProfile student, Opportunity opportunity) {
        EligibilityCheckResponse result = new EligibilityCheckResponse();
        List<String> reasons = new ArrayList<>();

        if (opportunity == null) {
            result.setEligible(false);
            reasons.add("Opportunity does not exist.");
            result.setReasons(reasons);
            return result;
        }

        // 1. Status & Deadline check
        if (opportunity.getStatus() != OpportunityStatus.PUBLISHED) {
            result.setEligible(false);
            reasons.add("This opportunity is currently " + opportunity.getStatus().name().toLowerCase() + " and not accepting applications.");
        }

        LocalDate today = LocalDate.now();
        if (opportunity.getDeadline() != null && opportunity.getDeadline().isBefore(today)) {
            result.setDeadlinePassed(true);
            result.setEligible(false);
            reasons.add("The application deadline passed on " + opportunity.getDeadline() + ".");
        } else {
            result.setDeadlinePassed(false);
        }

        if (student == null) {
            result.setEligible(false);
            result.setProfileIncomplete(true);
            reasons.add("Student profile information is missing. Please complete your profile first.");
            result.setReasons(reasons);
            return result;
        }

        // 2. Duplicate Application Check
        boolean alreadyApplied = applicationRepository.existsByStudentAndOpportunity(student, opportunity);
        result.setAlreadyApplied(alreadyApplied);
        if (alreadyApplied) {
            result.setEligible(false);
            reasons.add("You have already submitted an application for this opportunity.");
        }

        // 3. Profile Completeness Check
        boolean incomplete = false;
        if (student.getCgpa() == null) {
            reasons.add("CGPA is missing from your profile.");
            incomplete = true;
        }
        if (student.getDepartment() == null || student.getDepartment().isBlank()) {
            reasons.add("Department is not set in your profile.");
            incomplete = true;
        }
        if (student.getGraduationYear() == null) {
            reasons.add("Graduation year is not set in your profile.");
            incomplete = true;
        }
        result.setProfileIncomplete(incomplete);

        // 4. CGPA Requirement Check
        result.setStudentCgpa(student.getCgpa());
        result.setRequiredCgpa(opportunity.getMinCgpa());
        if (student.getCgpa() != null && opportunity.getMinCgpa() != null) {
            if (student.getCgpa().compareTo(opportunity.getMinCgpa()) >= 0) {
                result.setCgpaSatisfied(true);
            } else {
                result.setCgpaSatisfied(false);
                reasons.add("Your CGPA (" + student.getCgpa() + ") is below the minimum required CGPA (" + opportunity.getMinCgpa() + ").");
            }
        } else if (student.getCgpa() == null && opportunity.getMinCgpa() != null && opportunity.getMinCgpa().compareTo(BigDecimal.ZERO) > 0) {
            result.setCgpaSatisfied(false);
        } else {
            result.setCgpaSatisfied(true);
        }

        // 5. Department Eligibility Check
        result.setStudentDepartment(student.getDepartment());
        result.setEligibleDepartments(opportunity.getEligibleDepartments());
        if (opportunity.getEligibleDepartments() != null && !opportunity.getEligibleDepartments().isBlank()) {
            if (student.getDepartment() != null && !student.getDepartment().isBlank()) {
                String[] eligibleDepts = opportunity.getEligibleDepartments().split(",");
                boolean deptMatches = Arrays.stream(eligibleDepts)
                        .map(String::trim)
                        .anyMatch(d -> d.equalsIgnoreCase(student.getDepartment().trim()) ||
                                       student.getDepartment().toLowerCase().contains(d.toLowerCase()) ||
                                       d.toLowerCase().contains(student.getDepartment().toLowerCase()));
                result.setDepartmentSatisfied(deptMatches);
                if (!deptMatches) {
                    reasons.add("Your department (" + student.getDepartment() + ") is not eligible. Required: " + opportunity.getEligibleDepartments() + ".");
                }
            } else {
                result.setDepartmentSatisfied(false);
            }
        } else {
            result.setDepartmentSatisfied(true);
        }

        // 6. Graduation Year Check
        result.setStudentGraduationYear(student.getGraduationYear());
        result.setEligibleGraduationYears(opportunity.getEligibleGraduationYears());
        if (opportunity.getEligibleGraduationYears() != null && !opportunity.getEligibleGraduationYears().isBlank()) {
            if (student.getGraduationYear() != null) {
                String[] eligibleYears = opportunity.getEligibleGraduationYears().split(",");
                String studentYearStr = String.valueOf(student.getGraduationYear());
                boolean yearMatches = Arrays.stream(eligibleYears)
                        .map(String::trim)
                        .anyMatch(y -> y.equals(studentYearStr));
                result.setGraduationYearSatisfied(yearMatches);
                if (!yearMatches) {
                    reasons.add("Your graduation year (" + student.getGraduationYear() + ") is not eligible. Required: " + opportunity.getEligibleGraduationYears() + ".");
                }
            } else {
                result.setGraduationYearSatisfied(false);
            }
        } else {
            result.setGraduationYearSatisfied(true);
        }

        boolean overallEligible = reasons.isEmpty();
        result.setEligible(overallEligible);
        result.setReasons(reasons);
        return result;
    }
}
