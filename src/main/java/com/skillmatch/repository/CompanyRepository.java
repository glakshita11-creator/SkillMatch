package com.skillmatch.repository;

import com.skillmatch.entity.Company;
import com.skillmatch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByUser(User user);
    Optional<Company> findByUserId(Long userId);
}
