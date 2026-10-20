package com.skillmatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmatch.dto.ApplicationRequest;
import com.skillmatch.dto.ApplicationStatusUpdateRequest;
import com.skillmatch.entity.Application;
import com.skillmatch.entity.Opportunity;
import com.skillmatch.entity.StudentProfile;
import com.skillmatch.enums.ApplicationStatus;
import com.skillmatch.repository.ApplicationRepository;
import com.skillmatch.repository.OpportunityRepository;
import com.skillmatch.repository.StudentProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EligibilityAndApplicationWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    @DisplayName("8 & 9. Eligibility check and successful application submission")
    @WithMockUser(username = "priya.sharma@student.edu", roles = {"STUDENT"})
    void testEligibleApplicationSubmission() throws Exception {
        // Priya has CGPA 9.10, IT, 2025. Opportunity 2 is Cloud Platform Intern (min 7.00, IT, 2025/2026)
        Opportunity opp2 = opportunityRepository.findAll().stream()
                .filter(o -> o.getTitle().contains("Cloud Platform Intern"))
                .findFirst().orElseThrow();

        // Check eligibility API returns true
        mockMvc.perform(get("/api/opportunities/" + opp2.getId() + "/eligibility"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligible").value(true));

        // Submit application
        ApplicationRequest req = new ApplicationRequest("Passionate about DevOps and Cloud infrastructure.");
        mockMvc.perform(post("/api/opportunities/" + opp2.getId() + "/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("APPLIED"))
                .andExpect(jsonPath("$.data.studentName").value("Priya Sharma"));
    }

    @Test
    @DisplayName("10. Duplicate application to same opportunity is strictly rejected")
    @WithMockUser(username = "arun.kumar@student.edu", roles = {"STUDENT"})
    void testDuplicateApplicationRejection() throws Exception {
        // Arun Kumar already applied to Opportunity 1 in DataInitializer
        Opportunity opp1 = opportunityRepository.findAll().stream()
                .filter(o -> o.getTitle().contains("Associate Software Engineer"))
                .findFirst().orElseThrow();

        ApplicationRequest req = new ApplicationRequest("Second attempt to apply");
        mockMvc.perform(post("/api/opportunities/" + opp1.getId() + "/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("11. Deadline enforcement rejects expired opportunities")
    @WithMockUser(username = "priya.sharma@student.edu", roles = {"STUDENT"})
    void testDeadlineEnforcement() throws Exception {
        Opportunity opp1 = opportunityRepository.findAll().get(0);
        // Set past deadline
        opp1.setDeadline(LocalDate.now().minusDays(5));
        opportunityRepository.save(opp1);

        ApplicationRequest req = new ApplicationRequest("Applying after deadline");
        mockMvc.perform(post("/api/opportunities/" + opp1.getId() + "/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("12 & 13. Application status updates and unauthorized applicant access prevention")
    void testApplicationWorkflowAndAccess() throws Exception {
        Application app = applicationRepository.findAll().get(0);
        Long appId = app.getId();

        // 13: Another company (hr@cloudscale.io) cannot view applicants of TechCorp
        mockMvc.perform(get("/api/companies/me/opportunities/" + app.getOpportunity().getId() + "/applicants")
                        .with(user("hr@cloudscale.io").roles("COMPANY")))
                .andExpect(status().isForbidden());

        // 12: TechCorp recruiter updates status to INTERVIEW_SCHEDULED (from SHORTLISTED)
        ApplicationStatusUpdateRequest updateReq = new ApplicationStatusUpdateRequest(
                ApplicationStatus.INTERVIEW_SCHEDULED,
                "Technical round confirmed on Google Meet."
        );

        mockMvc.perform(patch("/api/applications/" + appId + "/status")
                        .with(user("recruiter@techcorp.com").roles("COMPANY"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INTERVIEW_SCHEDULED"))
                .andExpect(jsonPath("$.data.recruiterNotes").value("Technical round confirmed on Google Meet."));

        // Student views updated status
        mockMvc.perform(get("/api/applications/" + appId)
                        .with(user("arun.kumar@student.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INTERVIEW_SCHEDULED"));
    }
}
