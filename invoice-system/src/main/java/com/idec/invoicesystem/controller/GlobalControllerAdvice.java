package com.idec.invoicesystem.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import com.idec.invoicesystem.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private JobService jobService;

    @Value("${profit.password:IDEC2024}")
    private String profitPassword;

    @ModelAttribute("username")
    public String username(Authentication auth) {
        return auth != null ? auth.getName() : "User";
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @ModelAttribute("isStaff")
    public boolean isStaff(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
    }

    @ModelAttribute("userRole")
    public String userRole(Authentication auth) {
        if (auth == null) return "Guest";
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? "Administrator" : "Staff";
    }

    @ModelAttribute("profitPassword")
    public String profitPassword(Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? profitPassword : null;
    }

    @ModelAttribute("invoicePaidJobsCount")
    public long invoicePaidJobsCount(Authentication auth) {
        if (auth == null) return 0;
        try {
            return jobService.getInvoicePaidJobsCount();
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("invoicePendingJobsCount")
    public long invoicePendingJobsCount(Authentication auth) {
        if (auth == null) return 0;
        try {
            return jobService.getInvoicePendingJobsCount();
        } catch (Exception e) {
            return 0;
        }
    }
}
