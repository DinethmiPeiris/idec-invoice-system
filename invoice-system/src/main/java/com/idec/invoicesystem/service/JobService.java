package com.idec.invoicesystem.service;

import com.idec.invoicesystem.model.Job;
import com.idec.invoicesystem.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public List<Job> getAllJobs() {
        return jobRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Job> getJobById(String id) {
        return jobRepository.findById(id);
    }

    public Job saveJob(Job job) {
        if (job.getId() == null) {
            job.setCreatedAt(LocalDateTime.now());
        }
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    public void deleteJob(String id) {
        jobRepository.deleteById(id);
    }

    public List<Job> searchJobs(String query) {
        return jobRepository
                .findByJobNoContainingIgnoreCaseOrCompanyNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        query, query, query);
    }

    public List<Job> getJobsByStatus(String status) {
        return jobRepository.findByStatus(status);
    }

    public List<Job> getJobsByInvoiceStatus(String invoiceStatus) {
        if ("PENDING".equals(invoiceStatus)) {
            return jobRepository.findAllByOrderByCreatedAtDesc().stream()
                    .filter(j -> !"PAID".equals(j.getInvoiceStatus()))
                    .toList();
        }
        return jobRepository.findByInvoiceStatus(invoiceStatus);
    }

    public List<Job> getJobsByDateRange(LocalDate from, LocalDate to) {
        return jobRepository.findByDateBetweenOrderByDateAsc(from, to);
    }

    // ── Dashboard stats ─────────────────────────────────────────
    public long getTotalJobs()     { return jobRepository.count(); }
    public long getPendingJobs()   { return jobRepository.countByStatus("PENDING"); }
    public long getInProgressJobs(){ return jobRepository.countByStatus("IN_PROGRESS"); }
    public long getCompletedJobs() { return jobRepository.countByStatus("COMPLETED"); }
    public long getInvoicePaidJobsCount()   { return jobRepository.countByInvoiceStatus("PAID"); }
    public long getInvoicePendingJobsCount() { return jobRepository.count() - jobRepository.countByInvoiceStatus("PAID"); }

    public double getTotalBlAmount() {
        return jobRepository.findAll().stream()
                .mapToDouble(j -> j.getBlAmount() != null ? j.getBlAmount() : 0.0)
                .sum();
    }

    public double getTotalAdvance() {
        return jobRepository.findAll().stream()
                .mapToDouble(j -> j.getAdvance() != null ? j.getAdvance() : 0.0)
                .sum();
    }

    public double getTotalBalance() {
        return jobRepository.findAll().stream()
                .mapToDouble(j -> j.getBalance() != null ? j.getBalance() : 0.0)
                .sum();
    }

    public String getNextJobNo() {
        List<Job> latestJobs = jobRepository.findAllByOrderByCreatedAtDesc();
        if (latestJobs.isEmpty()) {
            return "001";
        }
        int maxNo = 0;
        boolean foundNumeric = false;
        for (Job j : latestJobs) {
            String jNo = j.getJobNo();
            if (jNo != null) {
                try {
                    int num = Integer.parseInt(jNo.trim());
                    if (num > maxNo) {
                        maxNo = num;
                        foundNumeric = true;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        if (foundNumeric) {
            return String.format("%03d", maxNo + 1);
        } else {
            String lastJobNo = latestJobs.get(0).getJobNo();
            try {
                return String.format("%03d", Integer.parseInt(lastJobNo.trim()) + 1);
            } catch (NumberFormatException e) {
                return "001";
            }
        }
    }
}
