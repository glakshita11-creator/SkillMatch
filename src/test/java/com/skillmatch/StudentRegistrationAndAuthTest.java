package com.skillmatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmatch.dto.AuthRequest;
import com.skillmatch.dto.ChangePasswordRequest;
import com.skillmatch.dto.StudentRegisterRequest;
import com.skillmatch.entity.User;
import com.skillmatch.enums.Role;
import com.skillmatch.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudentRegistrationAndAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("1. Student registration persists user and hashes password")
    void testStudentRegistration() throws Exception {
        StudentRegisterRequest req = new StudentRegisterRequest();
        req.setEmail("new.student@test.edu");
        req.setPassword("SecretPass@123");
        req.setFullName("Test Student");
        req.setPhone("+91 9999888877");
        req.setDepartment("Computer Science");
        req.setDegree("B.Tech");
        req.setGraduationYear(2025);
        req.setCgpa(new BigDecimal("8.50"));
        req.setSkills("Java, Spring Boot");

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("new.student@test.edu"))
                .andExpect(jsonPath("$.data.role").value("ROLE_STUDENT"));

        User savedUser = userRepository.findByEmail("new.student@test.edu").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(passwordEncoder.matches("SecretPass@123", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("2. Authentication with valid credentials succeeds and invalid fails")
    void testLoginBehavior() throws Exception {
        // Valid credentials
        AuthRequest validReq = new AuthRequest("admin@skillmatch.edu", "Admin@123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("admin@skillmatch.edu"));

        // Invalid password
        AuthRequest invalidReq = new AuthRequest("admin@skillmatch.edu", "WrongPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("3. Unauthorized access to protected endpoints is rejected")
    void testUnauthorizedEndpointAccess() throws Exception {
        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/companies/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4. Role-based access restrictions enforce boundaries")
    @WithMockUser(username = "arun.kumar@student.edu", roles = {"STUDENT"})
    void testRoleBasedAccessRestrictions() throws Exception {
        // Student accessing student endpoint -> OK
        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isOk());

        // Student attempting to access company endpoint -> Forbidden
        mockMvc.perform(get("/api/companies/me"))
                .andExpect(status().isForbidden());

        // Student attempting to access admin dashboard -> Forbidden
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. Password change validates current password and updates hash")
    @WithMockUser(username = "arun.kumar@student.edu", roles = {"STUDENT"})
    void testChangePassword() throws Exception {
        // Incorrect current password fails
        ChangePasswordRequest wrongReq = new ChangePasswordRequest("WrongPass@123", "BrandNewPass@123");
        mockMvc.perform(put("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Current password does not match."));


        // Correct current password succeeds
        ChangePasswordRequest correctReq = new ChangePasswordRequest("Password@123", "BrandNewPass@123");
        mockMvc.perform(put("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User updatedUser = userRepository.findByEmail("arun.kumar@student.edu").orElseThrow();
        assertThat(passwordEncoder.matches("BrandNewPass@123", updatedUser.getPassword())).isTrue();
    }
}

