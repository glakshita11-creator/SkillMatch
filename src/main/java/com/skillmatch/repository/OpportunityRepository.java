package com.skillmatch.repository;

import com.skillmatch.entity.Company;
import com.skillmatch.entity.Opportunity;
import com.skillmatch.enums.OpportunityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long>, JpaSpecificationExecutor<Opportunity> {

    List<Opportunity> findByCompany(Company company);

    List<Opportunity> findByCompanyId(Long companyId);

    List<Opportunity> findByStatus(OpportunityStatus status);

    long countByStatus(OpportunityStatus status);

    @Query("SELECT o FROM Opportunity o WHERE o.status = 'PUBLISHED' AND o.deadline >= CURRENT_DATE ORDER BY o.createdAt DESC")
    List<Opportunity> findActiveOpportunities();

    @Query("SELECT o FROM Opportunity o WHERE o.status = :status AND " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(o.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(o.company.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(o.requiredSkills) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(o.location) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Opportunity> searchByTextAndStatus(@Param("query") String query, @Param("status") OpportunityStatus status);
}
