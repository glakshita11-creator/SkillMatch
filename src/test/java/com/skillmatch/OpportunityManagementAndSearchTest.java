package com.skillmatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmatch.dto.OpportunityRequest;
import com.skillmatch.entity.Opportunity;
import com.skillmatch.enums.OpportunityStatus;
import com.skillmatch.enums.OpportunityType;
import com.skillmatch.enums.WorkplaceType;
import com.skillmatch.repository.OpportunityRepository;
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
class OpportunityManagementAndSearchTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Test
    @DisplayName("6. Opportunity creation succeeds and ownership restrictions are enforced")
    @WithMockUser(username = "recruiter@techcorp.com", roles = {"COMPANY"})
    void testOpportunityCreationAndOwnership() throws Exception {
        OpportunityRequest req = new OpportunityRequest();
        req.setTitle("Senior Backend Engineer");
        req.setDescription("Building high-throughput microservices in Java 21.");
        req.setOpportunityType(OpportunityType.FULL_TIME);
        req.setWorkplaceType(WorkplaceType.HYBRID);
        req.setLocation("Bengaluru");
        req.setRequiredSkills("Java, Spring, Kafka");
        req.setMinCgpa(new BigDecimal("8.00"));
        req.setEligibleDepartments("Computer Science");
        req.setEligibleGraduationYears("2025");
        req.setSalaryOrStipend("16 LPA");
        req.setOpenings(5);
        req.setDeadline(LocalDate.now().plusDays(30));
        req.setStatus(OpportunityStatus.PUBLISHED);

        // TechCorp creates opportunity -> OK
        String responseContent = mockMvc.perform(post("/api/opportunities")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Senior Backend Engineer"))
                .andReturn().getResponse().getContentAsString();

        // Check ownership restriction: another company (hr@cloudscale.io) cannot edit TechCorp's opportunity
        Long createdId = objectMapper.readTree(responseContent).path("data").path("id").asLong();

        // CloudScale attempts to update TechCorp's opportunity -> Forbidden
        req.setTitle("Hacked Title");
        mockMvc.perform(put("/api/opportunities/" + createdId)
                        .with(user("hr@cloudscale.io").roles("COMPANY"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7. Public opportunity search and filtering")
    void testOpportunitySearchAndFiltering() throws Exception {
        // Search by keyword "Java"
        mockMvc.perform(get("/api/opportunities?query=Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Filter by opportunity type INTERNSHIP
        mockMvc.perform(get("/api/opportunities?type=INTERNSHIP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].opportunityType").value("INTERNSHIP"));

        // Filter by location
        mockMvc.perform(get("/api/opportunities?location=Bengaluru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
