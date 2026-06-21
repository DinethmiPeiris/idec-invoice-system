package com.idec.invoicesystem.controller;

import com.idec.invoicesystem.service.JobService;
import com.idec.invoicesystem.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired private JobService jobService;
    @Autowired private CompanyService companyService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        boolean dbOk = true;
        try {
            model.addAttribute("totalJobs",      jobService.getTotalJobs());
            model.addAttribute("pendingJobs",    jobService.getPendingJobs());
            model.addAttribute("inProgressJobs", jobService.getInProgressJobs());
            model.addAttribute("completedJobs",  jobService.getCompletedJobs());
            model.addAttribute("totalBlAmount",  jobService.getTotalBlAmount());
            model.addAttribute("totalAdvance",   jobService.getTotalAdvance());
            model.addAttribute("totalBalance",   jobService.getTotalBalance());
            model.addAttribute("invoicePaidJobsCount",    jobService.getInvoicePaidJobsCount());
            model.addAttribute("invoicePendingJobsCount", jobService.getInvoicePendingJobsCount());
            model.addAttribute("recentJobs",     jobService.getAllJobs().stream().limit(10).toList());
        } catch (Exception e) {
            dbOk = false;
            model.addAttribute("totalJobs",      0L);
            model.addAttribute("pendingJobs",    0L);
            model.addAttribute("inProgressJobs", 0L);
            model.addAttribute("completedJobs",  0L);
            model.addAttribute("totalBlAmount",  0.0);
            model.addAttribute("totalAdvance",   0.0);
            model.addAttribute("totalBalance",   0.0);
            model.addAttribute("invoicePaidJobsCount",    0L);
            model.addAttribute("invoicePendingJobsCount", 0L);
            model.addAttribute("recentJobs",     List.of());
        }
        model.addAttribute("dbOk",     dbOk);
        model.addAttribute("username", auth != null ? auth.getName() : "User");
        return "dashboard";
    }
}

