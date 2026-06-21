package com.idec.invoicesystem.repository;

import com.idec.invoicesystem.model.Company;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends MongoRepository<Company, String> {
    List<Company> findByActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}
