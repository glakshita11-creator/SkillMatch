package com.skillmatch.service;

import com.skillmatch.dto.*;
import com.skillmatch.entity.Company;
import com.skillmatch.entity.StudentProfile;
import com.skillmatch.entity.User;
import com.skillmatch.enums.Role;
import com.skillmatch.exception.BadRequestException;
import com.skillmatch.exception.DuplicateResourceException;
import com.skillmatch.exception.ResourceNotFoundException;
import com.skillmatch.exception.UnauthorizedException;
import com.skillmatch.repository.CompanyRepository;
import com.skillmatch.repository.StudentProfileRepository;
import com.skillmatch.repository.UserRepository;
import com.skillmatch.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       StudentProfileRepository studentProfileRepository,
                       CompanyRepository companyRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse registerStudent(StudentRegisterRequest request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new DuplicateResourceException("An account with email " + cleanEmail + " already exists.");
        }

        User user = new User();
        user.setEmail(cleanEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setRole(Role.ROLE_STUDENT);
        user.setActive(true);
        user = userRepository.save(user);

        StudentProfile profile = new StudentProfile(user);
        profile.setDepartment(request.getDepartment());
        profile.setDegree(request.getDegree());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setCgpa(request.getCgpa());
        profile.setSkills(request.getSkills());
        profile.setResumeUrl(request.getResumeUrl());
        profile.setBio(request.getBio());
        profile = studentProfileRepository.save(profile);

        UserResponse response = toUserResponse(user);
        response.setProfileId(profile.getId());
        return response;
    }

    @Transactional
    public UserResponse registerCompany(CompanyRegisterRequest request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new DuplicateResourceException("An account with email " + cleanEmail + " already exists.");
        }

        User user = new User();
        user.setEmail(cleanEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setRole(Role.ROLE_COMPANY);
        user.setActive(true);
        user = userRepository.save(user);

        Company company = new Company(user, request.getCompanyName().trim());
        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setContactEmail(request.getContactEmail() != null ? request.getContactEmail().trim() : cleanEmail);
        company.setContactPhone(request.getContactPhone() != null ? request.getContactPhone().trim() : request.getPhone());
        company.setLocation(request.getLocation());
        company.setIndustry(request.getIndustry());
        company = companyRepository.save(company);

        UserResponse response = toUserResponse(user);
        response.setProfileId(company.getId());
        return response;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("User is not authenticated.");
        }
        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));
    }

    public UserResponse getCurrentUserResponse() {
        User user = getCurrentUser();
        UserResponse response = toUserResponse(user);
        if (user.getRole() == Role.ROLE_STUDENT) {
            studentProfileRepository.findByUserId(user.getId())
                    .ifPresent(p -> response.setProfileId(p.getId()));
        } else if (user.getRole() == Role.ROLE_COMPANY) {
            companyRepository.findByUserId(user.getId())
                    .ifPresent(c -> response.setProfileId(c.getId()));
        }
        return response;
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
