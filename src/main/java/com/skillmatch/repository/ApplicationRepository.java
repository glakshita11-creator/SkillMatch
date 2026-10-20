package com.skillmatch.repository;

import com.skillmatch.entity.Application;
import com.skillmatch.entity.Opportunity;
import com.skillmatch.entity.StudentProfile;
import com.skillmatch.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentOrderByAppliedAtDesc(StudentProfile student);

    List<Application> findByStudentIdOrderByAppliedAtDesc(Long studentProfileId);

    List<Application> findByOpportunityOrderByAppliedAtDesc(Opportunity opportunity);

    List<Application> findByOpportunityIdOrderByAppliedAtDesc(Long opportunityId);

    List<Application> findByOpportunityCompanyIdOrderByAppliedAtDesc(Long companyId);

    Optional<Application> findByStudentAndOpportunity(StudentProfile student, Opportunity opportunity);

    boolean existsByStudentAndOpportunity(StudentProfile student, Opportunity opportunity);

    boolean existsByStudentIdAndOpportunityId(Long studentProfileId, Long opportunityId);

    long countByStatus(ApplicationStatus status);

    long countByOpportunityCompanyId(Long companyId);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.opportunity.company.id = :companyId AND a.status = :status")
    long countByOpportunityCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") ApplicationStatus status);

    @Query("SELECT a.status, COUNT(a) FROM Application a GROUP BY a.status")
    List<Object[]> countApplicationsByStatus();
}
