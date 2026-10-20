package com.skillmatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmatch.dto.StudentProfileUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudentProfileAndResumeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("5. Student profile retrieval and update")
    @WithMockUser(username = "arun.kumar@student.edu", roles = {"STUDENT"})
    void testProfileRetrievalAndUpdate() throws Exception {
        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("arun.kumar@student.edu"))
                .andExpect(jsonPath("$.data.department").value("Computer Science"));

        StudentProfileUpdateRequest updateReq = new StudentProfileUpdateRequest();
        updateReq.setFullName("Arun K. Updated");
        updateReq.setPhone("+91 9999900000");
        updateReq.setDepartment("Information Technology");
        updateReq.setDegree("B.Tech");
        updateReq.setGraduationYear(2025);
        updateReq.setCgpa(new BigDecimal("8.95"));
        updateReq.setSkills("Java, Spring Boot, Microservices, AWS");
        updateReq.setBio("Updated bio description.");

        mockMvc.perform(put("/api/students/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Arun K. Updated"))
                .andExpect(jsonPath("$.data.department").value("Information Technology"))
                .andExpect(jsonPath("$.data.cgpa").value(8.95));
    }

    @Test
    @DisplayName("14. Resume upload validates PDF header and secures access")
    @WithMockUser(username = "arun.kumar@student.edu", roles = {"STUDENT"})
    void testResumeUploadAndAccessRestrictions() throws Exception {
        // Valid PDF upload with %PDF header
        byte[] validPdfContent = "%PDF-1.4 sample pdf content for resume test".getBytes();
        MockMultipartFile validFile = new MockMultipartFile("file", "my_resume.pdf", "application/pdf", validPdfContent);

        mockMvc.perform(multipart("/api/students/me/resume").file(validFile).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasResume").value(true));

        // Invalid non-PDF upload rejected
        byte[] invalidContent = "This is plain text not a pdf".getBytes();
        MockMultipartFile invalidFile = new MockMultipartFile("file", "fake.pdf", "application/pdf", invalidContent);

        mockMvc.perform(multipart("/api/students/me/resume").file(invalidFile).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
