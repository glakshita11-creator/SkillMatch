package com.skillmatch;

import com.skillmatch.dto.ApplicationResponse;
import com.skillmatch.enums.ApplicationStatus;
import com.skillmatch.util.CsvExporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CsvExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. CsvExporter handles RFC-4180 escaping and creates valid CSV")
    void testCsvExporterFormatting() {
        ApplicationResponse resp = new ApplicationResponse();
        resp.setId(101L);
        resp.setOpportunityTitle("Software Engineer, Backend");
        resp.setCompanyName("TechCorp \"International\"");
        resp.setStudentName("Arun Kumar");
        resp.setStudentEmail("arun.kumar@student.edu");
        resp.setStudentPhone("+91 9876543210");
        resp.setStudentDepartment("Computer Science");
        resp.setStudentDegree("B.Tech");
        resp.setStudentGraduationYear(2025);
        resp.setStudentCgpa(new BigDecimal("8.75"));
        resp.setStudentSkills("Java, Spring Boot, MySQL");
        resp.setStatus(ApplicationStatus.APPLIED);
        resp.setAppliedAt(LocalDateTime.of(2026, 10, 20, 10, 30));
        resp.setWithdrawn(false);

        String csvContent = CsvExporter.exportApplicationsToCsv(List.of(resp));

        assertThat(csvContent).contains("Application ID,Candidate Name,Email");
        assertThat(csvContent).contains("\"Software Engineer, Backend\"");
        assertThat(csvContent).contains("\"TechCorp \"\"International\"\"\"");
        assertThat(csvContent).contains("arun.kumar@student.edu");
        assertThat(csvContent).contains("8.75");
    }

    @Test
    @DisplayName("2. Company recruiter can export applicants CSV")
    @WithMockUser(username = "recruiter@techcorp.com", roles = {"COMPANY"})
    void testCompanyApplicantsCsvExport() throws Exception {
        mockMvc.perform(get("/api/companies/me/applicants/export-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Application ID,Candidate Name,Email")));
    }

    @Test
    @DisplayName("3. Administrator can export institution-wide applications CSV")
    @WithMockUser(username = "admin@skillmatch.edu", roles = {"ADMIN"})
    void testAdminApplicationsCsvExport() throws Exception {
        mockMvc.perform(get("/api/admin/applications/export-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"campus_placement_applications.csv\""))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Application ID,Candidate Name,Email")));
    }



    @Test
    @DisplayName("4. Student cannot export company or admin CSV data")
    @WithMockUser(username = "arun.kumar@student.edu", roles = {"STUDENT"})
    void testStudentForbiddenFromCsvExports() throws Exception {
        mockMvc.perform(get("/api/companies/me/applicants/export-csv"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/applications/export-csv"))
                .andExpect(status().isForbidden());
    }
}
