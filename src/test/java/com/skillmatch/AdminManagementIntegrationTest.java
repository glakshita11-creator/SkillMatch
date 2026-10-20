package com.skillmatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmatch.dto.CompanyRegisterRequest;
import com.skillmatch.entity.User;
import com.skillmatch.repository.UserRepository;
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

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("15. Administrator full access to dashboard, user control, and institutional data")
    @WithMockUser(username = "admin@skillmatch.edu", roles = {"ADMIN"})
    void testAdminFunctions() throws Exception {
        // Admin views aggregate metrics
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStudents").isNumber())
                .andExpect(jsonPath("$.data.totalCompanies").isNumber())
                .andExpect(jsonPath("$.data.placementRate").isNumber());

        // Admin views all users
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Admin registers a new company account
        CompanyRegisterRequest newComp = new CompanyRegisterRequest();
        newComp.setEmail("admin.created@company.com");
        newComp.setPassword("CompPass@123");
        newComp.setFullName("HR Executive");
        newComp.setCompanyName("Pioneer Technologies");
        newComp.setLocation("Mumbai");
        newComp.setIndustry("FinTech");

        mockMvc.perform(post("/api/admin/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newComp)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("admin.created@company.com"));

        // Admin toggles user status (deactivate / reactivate)
        User student = userRepository.findByEmail("priya.sharma@student.edu").orElseThrow();
        mockMvc.perform(patch("/api/admin/users/" + student.getId() + "/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }
}
