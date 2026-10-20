-- =============================================================================
-- SkillMatch: Sample Data Seed Script (Development and Demo Use Only)
-- Passwords for all accounts below: Password@123 (Admin is Admin@123)
-- BCrypt Hash:
--   Admin@123:    $2a$10$5sL.m6rN0zU8f8kG2yB57u8c3eJm9KjYcIqYgJ3uWv1t2e3r4t5y6
--   Password@123: $2a$10$7R9gH5kL8mP0qS2tU4vW6xY8zA1bC3dE5fG7hI9jK1lM3nO5pQ7rS
-- Note: When running the Spring Boot application, DataInitializer will also
-- bootstrap these records automatically if the database is fresh.
-- =============================================================================

USE skillmatch;

-- 1. Insert Administrator
INSERT INTO users (id, email, password, full_name, phone, role, active, created_at, updated_at)
VALUES (1, 'admin@skillmatch.edu', '$2a$10$lU72lqA9fIqWqVf93aXlDe0w5z4v2gG5qYmKjLhR1wT8uI9oP3aBq', 'College Placement Admin', '+91 9876543210', 'ROLE_ADMIN', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE email=email;

-- 2. Insert Companies
INSERT INTO users (id, email, password, full_name, phone, role, active, created_at, updated_at)
VALUES
(2, 'recruiter@techcorp.com', '$2a$10$4B9Y8W9Z3kK7xW1L6qA5UeJ8d9F7g6H5j4K3l2M1n0O9P8Q7R6S5T', 'Sarah Jenkins', '+91 9123456780', 'ROLE_COMPANY', TRUE, NOW(), NOW()),
(3, 'hr@cloudscale.io', '$2a$10$4B9Y8W9Z3kK7xW1L6qA5UeJ8d9F7g6H5j4K3l2M1n0O9P8Q7R6S5T', 'David Patel', '+91 9123456781', 'ROLE_COMPANY', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE email=email;

INSERT INTO companies (id, user_id, company_name, description, website, contact_email, contact_phone, location, industry, created_at, updated_at)
VALUES
(1, 2, 'TechCorp Solutions', 'Global enterprise software engineering and cloud transformation leader with over 50,000 employees worldwide.', 'https://techcorp.example.com', 'recruiter@techcorp.com', '+91 9123456780', 'Bengaluru, Karnataka', 'Information Technology', NOW(), NOW()),
(2, 3, 'CloudScale Systems', 'Next-generation DevOps, distributed systems, and AI cloud infrastructure services company.', 'https://cloudscale.example.com', 'hr@cloudscale.io', '+91 9123456781', 'Hyderabad, Telangana', 'Cloud Computing', NOW(), NOW())
ON DUPLICATE KEY UPDATE company_name=company_name;

-- 3. Insert Students
INSERT INTO users (id, email, password, full_name, phone, role, active, created_at, updated_at)
VALUES
(4, 'arun.kumar@student.edu', '$2a$10$4B9Y8W9Z3kK7xW1L6qA5UeJ8d9F7g6H5j4K3l2M1n0O9P8Q7R6S5T', 'Arun Kumar', '+91 9876501234', 'ROLE_STUDENT', TRUE, NOW(), NOW()),
(5, 'priya.sharma@student.edu', '$2a$10$4B9Y8W9Z3kK7xW1L6qA5UeJ8d9F7g6H5j4K3l2M1n0O9P8Q7R6S5T', 'Priya Sharma', '+91 9876501235', 'ROLE_STUDENT', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE email=email;

INSERT INTO student_profiles (id, user_id, department, degree, graduation_year, cgpa, skills, resume_file_name, resume_original_name, resume_content_type, resume_url, bio, created_at, updated_at)
VALUES
(1, 4, 'Computer Science', 'B.Tech', 2025, 8.75, 'Java, Spring Boot, MySQL, REST APIs, Git, React', NULL, NULL, NULL, 'https://linkedin.com/in/arunkumar-demo', 'Final-year CS undergraduate passionate about backend systems, distributed architectures, and API design.', NOW(), NOW()),
(2, 5, 'Information Technology', 'B.Tech', 2025, 9.10, 'Python, Java, Cloud Computing, Docker, Microservices, SQL', NULL, NULL, NULL, 'https://github.com/priyasharma-demo', 'Proactive student researcher with strong problem-solving skills and project experience in containerized backends.', NOW(), NOW())
ON DUPLICATE KEY UPDATE department=department;

-- 4. Insert Opportunities
INSERT INTO opportunities (id, company_id, title, description, opportunity_type, workplace_type, location, required_skills, min_cgpa, eligible_departments, eligible_graduation_years, salary_or_stipend, openings, deadline, status, created_at, updated_at)
VALUES
(1, 1, 'Associate Software Engineer (Java/Spring)', 'We are looking for motivated graduate engineers to join our Core Backend Platform Team. You will build scalable microservices, integrate relational and NoSQL databases, and work alongside senior architects.', 'FULL_TIME', 'HYBRID', 'Bengaluru', 'Java, Spring Boot, SQL, Git', 7.50, 'Computer Science, Information Technology, Electronics', '2025, 2026', '12.5 LPA', 15, DATE_ADD(CURRENT_DATE, INTERVAL 45 DAY), 'PUBLISHED', NOW(), NOW()),
(2, 1, 'Cloud Platform Intern', 'Exciting 6-month internship focused on cloud automation, CI/CD pipelines, and infrastructure as code. Exceptional interns will receive a pre-placement full-time offer (PPO).', 'INTERNSHIP', 'ON_SITE', 'Bengaluru', 'Linux, Docker, Python, Bash', 7.00, 'Computer Science, Information Technology', '2025, 2026', '35,000 / month', 8, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 'PUBLISHED', NOW(), NOW()),
(3, 2, 'Junior Distributed Systems Engineer', 'Work on high-throughput streaming systems, Kubernetes orchestration, and resilient microservice backends.', 'FULL_TIME', 'REMOTE', 'Hyderabad', 'Java, Go, Kubernetes, Kafka, MySQL', 8.00, 'Computer Science, Information Technology', '2024, 2025', '14.0 LPA', 5, DATE_ADD(CURRENT_DATE, INTERVAL 20 DAY), 'PUBLISHED', NOW(), NOW())
ON DUPLICATE KEY UPDATE title=title;

-- 5. Insert Sample Application
INSERT INTO applications (id, student_profile_id, opportunity_id, status, cover_note, recruiter_notes, interview_date, withdrawn, applied_at, updated_at)
VALUES
(1, 1, 1, 'SHORTLISTED', 'Excited about enterprise systems and backend development at TechCorp.', 'Strong academic record and solid Spring Boot project work. Progressed to technical round.', DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), FALSE, NOW(), NOW())
ON DUPLICATE KEY UPDATE status=status;
