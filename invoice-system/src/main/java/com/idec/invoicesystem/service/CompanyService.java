package com.idec.invoicesystem.service;

import com.idec.invoicesystem.model.Company;
import com.idec.invoicesystem.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public List<Company> getAllActiveCompanies() {
        return companyRepository.findByActiveTrueOrderByNameAsc();
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company saveCompany(Company company) {
        return companyRepository.save(company);
    }

    public Optional<Company> getCompanyById(String id) {
        return companyRepository.findById(id);
    }

    public void deleteCompany(String id) {
        companyRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return companyRepository.existsByNameIgnoreCase(name);
    }
}
