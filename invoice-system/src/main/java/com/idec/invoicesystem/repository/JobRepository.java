package com.idec.invoicesystem.repository;

import com.idec.invoicesystem.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {
    List<Job> findByStatus(String status);
    List<Job> findByCompanyIdOrderByCreatedAtDesc(String companyId);
    List<Job> findAllByOrderByCreatedAtDesc();
    List<Job> findByJobNoContainingIgnoreCaseOrCompanyNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrRemarksContainingIgnoreCase(
            String jobNo, String companyName, String description, String remarks);
    long countByStatus(String status);
    List<Job> findByInvoiceStatus(String invoiceStatus);
    long countByInvoiceStatus(String invoiceStatus);

    // Date-range query for summary report (uses the job's own date field)
    List<Job> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}
