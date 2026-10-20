-- =============================================================================
-- SkillMatch: Centralized Placement and Internship Management System
-- Database Schema for MySQL 8.x
-- =============================================================================

CREATE DATABASE IF NOT EXISTS skillmatch CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE skillmatch;

-- 1. Users Table (Authentication and Role-Based Access)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(25),
    role VARCHAR(30) NOT NULL, -- ROLE_STUDENT, ROLE_COMPANY, ROLE_ADMIN
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Student Profiles Table
CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    department VARCHAR(100),
    degree VARCHAR(100),
    graduation_year INT,
    cgpa DECIMAL(4,2),
    skills TEXT,
    resume_file_name VARCHAR(255),
    resume_original_name VARCHAR(255),
    resume_content_type VARCHAR(100),
    resume_url VARCHAR(500),
    bio TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_student_dept (department),
    INDEX idx_student_grad_year (graduation_year),
    INDEX idx_student_cgpa (cgpa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Companies Table
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    company_name VARCHAR(200) NOT NULL,
    description TEXT,
    website VARCHAR(255),
    contact_email VARCHAR(150),
    contact_phone VARCHAR(25),
    location VARCHAR(150),
    industry VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_company_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_company_name (company_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Opportunities Table (Jobs and Internships)
CREATE TABLE IF NOT EXISTS opportunities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description MEDIUMTEXT NOT NULL,
    opportunity_type VARCHAR(30) NOT NULL, -- FULL_TIME, INTERNSHIP, PART_TIME
    workplace_type VARCHAR(30) NOT NULL DEFAULT 'ON_SITE', -- ON_SITE, HYBRID, REMOTE
    location VARCHAR(150) NOT NULL,
    required_skills TEXT,
    min_cgpa DECIMAL(4,2) NOT NULL DEFAULT 0.00,
    eligible_departments TEXT, -- Comma-separated (e.g. "Computer Science, Information Technology")
    eligible_graduation_years VARCHAR(255), -- Comma-separated (e.g. "2025, 2026")
    salary_or_stipend VARCHAR(100),
    openings INT NOT NULL DEFAULT 1,
    deadline DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED', -- DRAFT, PUBLISHED, CLOSED
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_opp_company FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    INDEX idx_opp_status (status),
    INDEX idx_opp_deadline (deadline),
    INDEX idx_opp_type (opportunity_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Applications Table
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_profile_id BIGINT NOT NULL,
    opportunity_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'APPLIED', -- APPLIED, UNDER_REVIEW, SHORTLISTED, INTERVIEW_SCHEDULED, SELECTED, REJECTED
    cover_note TEXT,
    recruiter_notes TEXT,
    interview_date DATETIME,
    withdrawn BOOLEAN NOT NULL DEFAULT FALSE,
    applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_student FOREIGN KEY (student_profile_id) REFERENCES student_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_app_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities (id) ON DELETE CASCADE,
    CONSTRAINT uk_student_opportunity UNIQUE (student_profile_id, opportunity_id),
    INDEX idx_app_status (status),
    INDEX idx_app_applied_at (applied_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
