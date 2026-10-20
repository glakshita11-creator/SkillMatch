package com.skillmatch.config;

import com.skillmatch.entity.*;
import com.skillmatch.enums.*;
import com.skillmatch.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyRepository companyRepository;
    private final OpportunityRepository opportunityRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentProfileRepository studentProfileRepository,
                           CompanyRepository companyRepository,
                           OpportunityRepository opportunityRepository,
                           ApplicationRepository applicationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyRepository = companyRepository;
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized with {} user accounts.", userRepository.count());
            return;
        }

        log.info("Bootstrapping SkillMatch database with initial demo accounts and opportunities...");

        // 1. Admin Account
        User admin = new User(
                "admin@skillmatch.edu",
                passwordEncoder.encode("Admin@123"),
                "College Placement Administrator",
                "+91 9876543210",
                Role.ROLE_ADMIN
        );
        userRepository.save(admin);

        // 2. Company 1 - TechCorp Solutions
        User compUser1 = new User(
                "recruiter@techcorp.com",
                passwordEncoder.encode("Password@123"),
                "Sarah Jenkins",
                "+91 9123456780",
                Role.ROLE_COMPANY
        );
        userRepository.save(compUser1);

        Company comp1 = new Company(compUser1, "TechCorp Solutions");
        comp1.setDescription("Global enterprise software engineering and cloud transformation leader with over 50,000 employees worldwide.");
        comp1.setWebsite("https://techcorp.example.com");
        comp1.setContactEmail("recruiter@techcorp.com");
        comp1.setContactPhone("+91 9123456780");
        comp1.setLocation("Bengaluru, Karnataka");
        comp1.setIndustry("Information Technology");
        comp1 = companyRepository.save(comp1);

        // 3. Company 2 - CloudScale Systems
        User compUser2 = new User(
                "hr@cloudscale.io",
                passwordEncoder.encode("Password@123"),
                "David Patel",
                "+91 9123456781",
                Role.ROLE_COMPANY
        );
        userRepository.save(compUser2);

        Company comp2 = new Company(compUser2, "CloudScale Systems");
        comp2.setDescription("Next-generation DevOps, distributed systems, and AI cloud infrastructure services company.");
        comp2.setWebsite("https://cloudscale.example.com");
        comp2.setContactEmail("hr@cloudscale.io");
        comp2.setContactPhone("+91 9123456781");
        comp2.setLocation("Hyderabad, Telangana");
        comp2.setIndustry("Cloud Computing");
        comp2 = companyRepository.save(comp2);

        // 4. Student 1 - Arun Kumar
        User stuUser1 = new User(
                "arun.kumar@student.edu",
                passwordEncoder.encode("Password@123"),
                "Arun Kumar",
                "+91 9876501234",
                Role.ROLE_STUDENT
        );
        userRepository.save(stuUser1);

        StudentProfile stuProfile1 = new StudentProfile(stuUser1);
        stuProfile1.setDepartment("Computer Science");
        stuProfile1.setDegree("B.Tech");
        stuProfile1.setGraduationYear(2025);
        stuProfile1.setCgpa(new BigDecimal("8.75"));
        stuProfile1.setSkills("Java, Spring Boot, MySQL, REST APIs, Git, React");
        stuProfile1.setResumeUrl("https://linkedin.com/in/arunkumar-demo");
        stuProfile1.setBio("Final-year CS undergraduate passionate about backend systems, distributed architectures, and API design.");
        stuProfile1 = studentProfileRepository.save(stuProfile1);

        // 5. Student 2 - Priya Sharma
        User stuUser2 = new User(
                "priya.sharma@student.edu",
                passwordEncoder.encode("Password@123"),
                "Priya Sharma",
                "+91 9876501235",
                Role.ROLE_STUDENT
        );
        userRepository.save(stuUser2);

        StudentProfile stuProfile2 = new StudentProfile(stuUser2);
        stuProfile2.setDepartment("Information Technology");
        stuProfile2.setDegree("B.Tech");
        stuProfile2.setGraduationYear(2025);
        stuProfile2.setCgpa(new BigDecimal("9.10"));
        stuProfile2.setSkills("Python, Java, Cloud Computing, Docker, Microservices, SQL");
        stuProfile2.setResumeUrl("https://github.com/priyasharma-demo");
        stuProfile2.setBio("Proactive student researcher with strong problem-solving skills and project experience in containerized backends.");
        stuProfile2 = studentProfileRepository.save(stuProfile2);

        // 6. Opportunities
        Opportunity opp1 = new Opportunity();
        opp1.setCompany(comp1);
        opp1.setTitle("Associate Software Engineer (Java/Spring)");
        opp1.setDescription("We are looking for motivated graduate engineers to join our Core Backend Platform Team. You will build scalable microservices, integrate relational and NoSQL databases, and work alongside senior architects.");
        opp1.setOpportunityType(OpportunityType.FULL_TIME);
        opp1.setWorkplaceType(WorkplaceType.HYBRID);
        opp1.setLocation("Bengaluru, Karnataka");
        opp1.setRequiredSkills("Java, Spring Boot, SQL, Git");
        opp1.setMinCgpa(new BigDecimal("7.50"));
        opp1.setEligibleDepartments("Computer Science, Information Technology, Electronics");
        opp1.setEligibleGraduationYears("2025, 2026");
        opp1.setSalaryOrStipend("12.5 LPA");
        opp1.setOpenings(15);
        opp1.setDeadline(LocalDate.now().plusDays(45));
        opp1.setStatus(OpportunityStatus.PUBLISHED);
        opp1 = opportunityRepository.save(opp1);

        Opportunity opp2 = new Opportunity();
        opp2.setCompany(comp1);
        opp2.setTitle("Cloud Platform Intern");
        opp2.setDescription("Exciting 6-month internship focused on cloud automation, CI/CD pipelines, and infrastructure as code. Exceptional interns will receive a pre-placement full-time offer (PPO).");
        opp2.setOpportunityType(OpportunityType.INTERNSHIP);
        opp2.setWorkplaceType(WorkplaceType.ON_SITE);
        opp2.setLocation("Bengaluru, Karnataka");
        opp2.setRequiredSkills("Linux, Docker, Python, Bash");
        opp2.setMinCgpa(new BigDecimal("7.00"));
        opp2.setEligibleDepartments("Computer Science, Information Technology");
        opp2.setEligibleGraduationYears("2025, 2026");
        opp2.setSalaryOrStipend("35,000 / month");
        opp2.setOpenings(8);
        opp2.setDeadline(LocalDate.now().plusDays(30));
        opp2.setStatus(OpportunityStatus.PUBLISHED);
        opp2 = opportunityRepository.save(opp2);

        Opportunity opp3 = new Opportunity();
        opp3.setCompany(comp2);
        opp3.setTitle("Junior Distributed Systems Engineer");
        opp3.setDescription("Work on high-throughput streaming systems, Kubernetes orchestration, and resilient microservice backends.");
        opp3.setOpportunityType(OpportunityType.FULL_TIME);
        opp3.setWorkplaceType(WorkplaceType.REMOTE);
        opp3.setLocation("Hyderabad, Telangana");
        opp3.setRequiredSkills("Java, Go, Kubernetes, Kafka, MySQL");
        opp3.setMinCgpa(new BigDecimal("8.00"));
        opp3.setEligibleDepartments("Computer Science, Information Technology");
        opp3.setEligibleGraduationYears("2024, 2025");
        opp3.setSalaryOrStipend("14.0 LPA");
        opp3.setOpenings(5);
        opp3.setDeadline(LocalDate.now().plusDays(20));
        opp3.setStatus(OpportunityStatus.PUBLISHED);
        opp3 = opportunityRepository.save(opp3);

        // 7. Sample Application
        Application app1 = new Application();
        app1.setStudent(stuProfile1);
        app1.setOpportunity(opp1);
        app1.setStatus(ApplicationStatus.SHORTLISTED);
        app1.setCoverNote("Excited about enterprise systems and backend development at TechCorp.");
        app1.setRecruiterNotes("Strong academic record and solid Spring Boot project work. Progressed to technical round.");
        app1.setInterviewDate(LocalDate.now().plusDays(7).atTime(14, 0));
        app1.setWithdrawn(false);
        applicationRepository.save(app1);

        log.info("SkillMatch initial bootstrap complete: 1 Admin, 2 Companies, 2 Students, 3 Opportunities, 1 Application created.");
    }
}
