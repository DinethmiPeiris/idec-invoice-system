package com.idec.invoicesystem.controller;

import com.idec.invoicesystem.model.Job;
import com.idec.invoicesystem.dto.InvoiceGenerateRequest;
import com.idec.invoicesystem.service.CompanyService;
import com.idec.invoicesystem.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.idec.invoicesystem.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/jobs")
public class JobController {

    @Autowired private JobService jobService;
    @Autowired private CompanyService companyService;
    @Autowired private PdfService pdfService;

    // ── All Jobs ─────────────────────────────────────────────────
    @GetMapping
    public String allJobs(@RequestParam(required = false) String search,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String invoiceStatus,
                          Model model, Authentication auth) {
        boolean dbOk = true;
        try {
            var jobs = (search != null && !search.isBlank())
                    ? jobService.searchJobs(search)
                    : (status != null && !status.isBlank())
                        ? jobService.getJobsByStatus(status)
                        : (invoiceStatus != null && !invoiceStatus.isBlank())
                            ? jobService.getJobsByInvoiceStatus(invoiceStatus)
                            : jobService.getAllJobs();
            model.addAttribute("jobs", jobs);
        } catch (Exception e) {
            dbOk = false;
            model.addAttribute("jobs", java.util.List.of());
        }
        model.addAttribute("dbOk", dbOk);
        model.addAttribute("search", search);
        model.addAttribute("statusFilter", status);
        model.addAttribute("invoiceStatusFilter", invoiceStatus);
        model.addAttribute("username", auth != null ? auth.getName() : "User");
        return "jobs/all-jobs";
    }

    // ── Add Job Form ─────────────────────────────────────────────
    @GetMapping("/add")
    public String addJobForm(Model model, Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/jobs";
        }

        Job job = new Job();
        job.setDate(LocalDate.now());
        try {
            job.setJobNo(jobService.getNextJobNo());
        } catch (Exception e) {
            job.setJobNo("");
        }
        model.addAttribute("job", job);
        try {
            model.addAttribute("companies", companyService.getAllActiveCompanies());
        } catch (Exception e) {
            model.addAttribute("companies", java.util.List.of());
            model.addAttribute("dbWarning", "Database unreachable. Please fix MongoDB Atlas network access first.");
        }
        boolean isStaff = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStaff", isStaff);
        model.addAttribute("username", auth != null ? auth.getName() : "User");
        model.addAttribute("isEdit", false);
        return "jobs/add-job";
    }

    @PostMapping("/add")
    public String addJob(@ModelAttribute Job job, RedirectAttributes ra, Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/jobs";
        }

        if (job.getCompanyId() != null && !job.getCompanyId().isBlank()) {
            companyService.getCompanyById(job.getCompanyId())
                    .ifPresent(c -> job.setCompanyName(c.getName()));
        }
        // Initialize Section C fields if null
        if (job.getDoCharges() == null) job.setDoCharges(0.0);
        if (job.getEntryPassing() == null) job.setEntryPassing(0.0);
        if (job.getDeliveryExpenses() == null) job.setDeliveryExpenses(0.0);
        if (job.getCommission() == null) job.setCommission(0.0);
        if (job.getHipgCharges() == null) job.setHipgCharges(0.0);
        if (job.getHandlingExpenses() == null) job.setHandlingExpenses(0.0);
        if (job.getOther() == null) job.setOther(0.0);
        if (job.getAgencyFee() == null) job.setAgencyFee(0.0);
        if (job.getCustomChargeName() == null) job.setCustomChargeName("");
        if (job.getCustomChargeValue() == null) job.setCustomChargeValue(0.0);
        if (job.getBlAmount() == null) job.setBlAmount(0.0);
        if (job.getAdvance() == null) job.setAdvance(0.0);
        if (job.getBalance() == null) job.setBalance(0.0);

        job.setStatus("PENDING");
        job.setInvoiceStatus("PENDING");
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        jobService.saveJob(job);
        ra.addFlashAttribute("success", "Job #" + job.getJobNo() + " added successfully!");
        return "redirect:/jobs";
    }

    // ── Edit Job Form ─────────────────────────────────────────────
    @GetMapping("/edit/{id}")
    public String editJobForm(@PathVariable String id, Model model, Authentication auth) {
        Optional<Job> job = jobService.getJobById(id);
        if (job.isEmpty()) return "redirect:/jobs";
        model.addAttribute("job", job.get());
        model.addAttribute("companies", companyService.getAllActiveCompanies());
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStaff", isStaff);
        model.addAttribute("username", auth != null ? auth.getName() : "User");
        model.addAttribute("isEdit", true);
        return "jobs/add-job";
    }

    @PostMapping("/edit/{id}")
    public String editJob(@PathVariable String id, @ModelAttribute Job submittedJob, RedirectAttributes ra, Authentication auth) {
        Optional<Job> existingOpt = jobService.getJobById(id);
        if (existingOpt.isEmpty()) return "redirect:/jobs";
        Job existingJob = existingOpt.get();

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Copy Section A & B fields
        existingJob.setJobNo(submittedJob.getJobNo());
        existingJob.setDate(submittedJob.getDate());
        existingJob.setCompanyId(submittedJob.getCompanyId());
        if (submittedJob.getCompanyId() != null && !submittedJob.getCompanyId().isBlank()) {
            companyService.getCompanyById(submittedJob.getCompanyId())
                    .ifPresent(c -> existingJob.setCompanyName(c.getName()));
        } else {
            existingJob.setCompanyName(null);
        }
        existingJob.setShipper(submittedJob.getShipper());
        existingJob.setVesselName(submittedJob.getVesselName());
        existingJob.setDescription(submittedJob.getDescription());
        existingJob.setChassisContainerNo(submittedJob.getChassisContainerNo());
        existingJob.setBlNumber(submittedJob.getBlNumber());
        existingJob.setInvoiceNo(submittedJob.getInvoiceNo());
        existingJob.setCustomsRegister(submittedJob.getCustomsRegister());
        existingJob.setEntrySubmit(submittedJob.getEntrySubmit());
        existingJob.setDriveType(submittedJob.getDriveType());
        existingJob.setDuty(submittedJob.getDuty());
        existingJob.setDeliveryDate(submittedJob.getDeliveryDate());
        existingJob.setRemarks(submittedJob.getRemarks());
        existingJob.setStatus(submittedJob.getStatus());
        existingJob.setInvoiceStatus(submittedJob.getInvoiceStatus());

        // Copy LKR charge fields
        existingJob.setDoCharges(submittedJob.getDoCharges());
        existingJob.setCustomDutyAmount(submittedJob.getCustomDutyAmount());
        existingJob.setHipgCharges(submittedJob.getHipgCharges());
        existingJob.setCictCharges(submittedJob.getCictCharges());
        existingJob.setDoExtension(submittedJob.getDoExtension());
        existingJob.setValuationExpences(submittedJob.getValuationExpences());
        existingJob.setPlantQuarantine(submittedJob.getPlantQuarantine());
        existingJob.setRctCharges(submittedJob.getRctCharges());
        existingJob.setImportControlDebit(submittedJob.getImportControlDebit());
        existingJob.setSlpaBondingCharges(submittedJob.getSlpaBondingCharges());
        existingJob.setAmendmentCharges(submittedJob.getAmendmentCharges());
        existingJob.setHandlingExpenses(submittedJob.getHandlingExpenses());
        existingJob.setDocumentationExpences(submittedJob.getDocumentationExpences());
        existingJob.setExamination(submittedJob.getExamination());
        existingJob.setWeighBridgeCharges(submittedJob.getWeighBridgeCharges());
        existingJob.setLabour(submittedJob.getLabour());
        existingJob.setAdditional(submittedJob.getAdditional());
        existingJob.setVatRegistration(submittedJob.getVatRegistration());
        existingJob.setTradeRegistration(submittedJob.getTradeRegistration());
        existingJob.setTransport(submittedJob.getTransport());
        existingJob.setOther(submittedJob.getOther());
        existingJob.setAgencyFee(submittedJob.getAgencyFee());
        existingJob.setCustomChargeName(submittedJob.getCustomChargeName());
        existingJob.setCustomChargeValue(submittedJob.getCustomChargeValue());

        // Copy cost fields
        existingJob.setEntryPassing(submittedJob.getEntryPassing());
        existingJob.setDeliveryExpenses(submittedJob.getDeliveryExpenses());
        existingJob.setCommission(submittedJob.getCommission());

        // Save calculated totals
        existingJob.setBlAmount(submittedJob.getBlAmount());
        existingJob.setBalance(submittedJob.getBalance());

        // Only Admin can submit/change the Advance field
        if (isAdmin) {
            existingJob.setAdvance(submittedJob.getAdvance());
            if ("PENDING".equals(existingJob.getStatus())) {
                existingJob.setStatus("IN_PROGRESS");
            }
        }

        existingJob.setUpdatedAt(LocalDateTime.now());
        jobService.saveJob(existingJob);
        ra.addFlashAttribute("success", "Job #" + existingJob.getJobNo() + " updated successfully!");
        return "redirect:/jobs";
    }

    // ── Delete Job ───────────────────────────────────────────────
    @PostMapping("/delete/{id}")
    public String deleteJob(@PathVariable String id, RedirectAttributes ra) {
        jobService.deleteJob(id);
        ra.addFlashAttribute("success", "Job deleted successfully.");
        return "redirect:/jobs";
    }

    // ── Update Invoice Status (Admin Only) ───────────────────────
    @PostMapping("/invoice-status/{id}")
    public String updateInvoiceStatus(@PathVariable String id, @RequestParam String invoiceStatus, RedirectAttributes ra, Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return "redirect:/jobs/view/" + id;
        }

        Optional<Job> jobOpt = jobService.getJobById(id);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            job.setInvoiceStatus(invoiceStatus);
            job.setUpdatedAt(LocalDateTime.now());
            jobService.saveJob(job);
            ra.addFlashAttribute("success", "Invoice status for Job #" + job.getJobNo() + " updated to " + invoiceStatus + ".");
        }
        return "redirect:/jobs/view/" + id;
    }

    // ── Summary Report (Admin Only) ──────────────────────────────
    @GetMapping("/report")
    public String summaryReport(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Model model, Authentication auth) {

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) return "redirect:/jobs";

        model.addAttribute("username", auth != null ? auth.getName() : "User");
        model.addAttribute("isAdmin", true);

        if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate   = LocalDate.parse(to);
            List<Job> jobs = jobService.getJobsByDateRange(fromDate, toDate);

            double totalDoCharges         = jobs.stream().mapToDouble(j -> j.getDoCharges()         != null ? j.getDoCharges()         : 0.0).sum();
            double totalEntryPassing      = jobs.stream().mapToDouble(j -> j.getEntryPassing()      != null ? j.getEntryPassing()      : 0.0).sum();
            double totalDeliveryExpenses  = jobs.stream().mapToDouble(j -> j.getDeliveryExpenses()  != null ? j.getDeliveryExpenses()  : 0.0).sum();
            double totalCommission        = jobs.stream().mapToDouble(j -> j.getCommission()        != null ? j.getCommission()        : 0.0).sum();
            double totalHipgCharges       = jobs.stream().mapToDouble(j -> j.getHipgCharges()       != null ? j.getHipgCharges()       : 0.0).sum();
            double totalHandlingExpenses  = jobs.stream().mapToDouble(j -> j.getHandlingExpenses()  != null ? j.getHandlingExpenses()  : 0.0).sum();
            double totalOther             = jobs.stream().mapToDouble(j -> j.getOther()             != null ? j.getOther()             : 0.0).sum();
            double totalAgencyFee         = jobs.stream().mapToDouble(j -> j.getAgencyFee()         != null ? j.getAgencyFee()         : 0.0).sum();
            double totalBl                = jobs.stream().mapToDouble(j -> j.getBlAmount()          != null ? j.getBlAmount()          : 0.0).sum();
            double totalAdvance           = jobs.stream().mapToDouble(j -> j.getAdvance()           != null ? j.getAdvance()           : 0.0).sum();
            double totalBalance           = jobs.stream().mapToDouble(j -> j.getBalance()           != null ? j.getBalance()           : 0.0).sum();

            model.addAttribute("jobs",                  jobs);
            model.addAttribute("totalDoCharges",        totalDoCharges);
            model.addAttribute("totalEntryPassing",     totalEntryPassing);
            model.addAttribute("totalDeliveryExpenses", totalDeliveryExpenses);
            model.addAttribute("totalCommission",       totalCommission);
            model.addAttribute("totalHipgCharges",      totalHipgCharges);
            model.addAttribute("totalHandlingExpenses", totalHandlingExpenses);
            model.addAttribute("totalOther",            totalOther);
            model.addAttribute("totalAgencyFee",        totalAgencyFee);
            model.addAttribute("totalBl",               totalBl);
            model.addAttribute("totalAdvance",          totalAdvance);
            model.addAttribute("totalBalance",          totalBalance);
            model.addAttribute("fromDate",              from);
            model.addAttribute("toDate",                to);
            model.addAttribute("generated",             true);
        } else {
            model.addAttribute("generated", false);
        }

        return "jobs/report";
    }

    // ── Summary Report PDF (Admin Only) ──────────────────────────────────────
    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> summaryReportPdf(
            @RequestParam String from,
            @RequestParam String to,
            Authentication auth) {

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate   = LocalDate.parse(to);
        List<Job> jobs = jobService.getJobsByDateRange(fromDate, toDate);

        double totalDoCharges        = jobs.stream().mapToDouble(j -> j.getDoCharges()        != null ? j.getDoCharges()        : 0.0).sum();
        double totalEntryPassing     = jobs.stream().mapToDouble(j -> j.getEntryPassing()     != null ? j.getEntryPassing()     : 0.0).sum();
        double totalDeliveryExpenses = jobs.stream().mapToDouble(j -> j.getDeliveryExpenses() != null ? j.getDeliveryExpenses() : 0.0).sum();
        double totalCommission       = jobs.stream().mapToDouble(j -> j.getCommission()       != null ? j.getCommission()       : 0.0).sum();
        double totalHipgCharges      = jobs.stream().mapToDouble(j -> j.getHipgCharges()      != null ? j.getHipgCharges()      : 0.0).sum();
        double totalHandlingExpenses = jobs.stream().mapToDouble(j -> j.getHandlingExpenses() != null ? j.getHandlingExpenses() : 0.0).sum();
        double totalOther            = jobs.stream().mapToDouble(j -> j.getOther()            != null ? j.getOther()            : 0.0).sum();
        double totalAgencyFee        = jobs.stream().mapToDouble(j -> j.getAgencyFee()        != null ? j.getAgencyFee()        : 0.0).sum();
        double totalBl               = jobs.stream().mapToDouble(j -> j.getBlAmount()         != null ? j.getBlAmount()         : 0.0).sum();
        double totalAdvance          = jobs.stream().mapToDouble(j -> j.getAdvance()          != null ? j.getAdvance()          : 0.0).sum();
        double totalBalance          = jobs.stream().mapToDouble(j -> j.getBalance()          != null ? j.getBalance()          : 0.0).sum();

        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariable("jobs",                  jobs);
        context.setVariable("fromDate",              from);
        context.setVariable("toDate",                to);
        context.setVariable("totalDoCharges",        totalDoCharges);
        context.setVariable("totalEntryPassing",     totalEntryPassing);
        context.setVariable("totalDeliveryExpenses", totalDeliveryExpenses);
        context.setVariable("totalCommission",       totalCommission);
        context.setVariable("totalHipgCharges",      totalHipgCharges);
        context.setVariable("totalHandlingExpenses", totalHandlingExpenses);
        context.setVariable("totalOther",            totalOther);
        context.setVariable("totalAgencyFee",        totalAgencyFee);
        context.setVariable("totalBl",               totalBl);
        context.setVariable("totalAdvance",          totalAdvance);
        context.setVariable("totalBalance",          totalBalance);

        try {
            byte[] pdfBytes = pdfService.generatePdf("jobs/report-pdf", context);
            String filename = "SummaryReport_" + from + "_to_" + to + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename(filename).build());
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── Profit Report (Admin Only) ──────────────────────────────
    @GetMapping("/report/profit")
    public String profitReport(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Model model, Authentication auth) {

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) return "redirect:/jobs";

        model.addAttribute("username", auth != null ? auth.getName() : "User");
        model.addAttribute("isAdmin", true);

        if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate   = LocalDate.parse(to);
            List<Job> jobs = jobService.getJobsByDateRange(fromDate, toDate);

            // Filter for COMPLETED jobs
            List<Job> completedJobs = new java.util.ArrayList<>();
            double totalInvoiceAmount = 0.0;
            double totalExpensesAmount = 0.0;
            double totalProfitAmount = 0.0;

            for (Job j : jobs) {
                if ("COMPLETED".equalsIgnoreCase(j.getStatus())) {
                    // Calculate expenses
                    double expenses = calculateJobExpenses(j);
                    j.setTempExpenses(expenses);

                    // Invoice total is blAmount
                    double invoiceTotal = j.getBlAmount() != null ? j.getBlAmount() : 0.0;
                    double profit = invoiceTotal - expenses;
                    j.setTempProfit(profit);

                    // Lookup TIN No
                    if (j.getCompanyId() != null && !j.getCompanyId().isBlank()) {
                        companyService.getCompanyById(j.getCompanyId()).ifPresent(c -> {
                            j.setTempTinNo(c.getTinNo());
                        });
                    }

                    completedJobs.add(j);

                    totalInvoiceAmount += invoiceTotal;
                    totalExpensesAmount += expenses;
                    totalProfitAmount += profit;
                }
            }

            model.addAttribute("jobs", completedJobs);
            model.addAttribute("totalInvoiceAmount", totalInvoiceAmount);
            model.addAttribute("totalExpensesAmount", totalExpensesAmount);
            model.addAttribute("totalProfitAmount", totalProfitAmount);
            model.addAttribute("fromDate", from);
            model.addAttribute("toDate", to);
            model.addAttribute("generated", true);
        } else {
            model.addAttribute("generated", false);
        }

        return "jobs/report-profit";
    }

    // ── Profit Report PDF (Admin Only) ──────────────────────────────────────
    @GetMapping("/report/profit/pdf")
    public ResponseEntity<byte[]> profitReportPdf(
            @RequestParam String from,
            @RequestParam String to,
            Authentication auth) {

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate   = LocalDate.parse(to);
        List<Job> jobs = jobService.getJobsByDateRange(fromDate, toDate);

        List<Job> completedJobs = new java.util.ArrayList<>();
        double totalInvoiceAmount = 0.0;
        double totalExpensesAmount = 0.0;
        double totalProfitAmount = 0.0;

        for (Job j : jobs) {
            if ("COMPLETED".equalsIgnoreCase(j.getStatus())) {
                double expenses = calculateJobExpenses(j);
                j.setTempExpenses(expenses);

                double invoiceTotal = j.getBlAmount() != null ? j.getBlAmount() : 0.0;
                double profit = invoiceTotal - expenses;
                j.setTempProfit(profit);

                if (j.getCompanyId() != null && !j.getCompanyId().isBlank()) {
                    companyService.getCompanyById(j.getCompanyId()).ifPresent(c -> {
                        j.setTempTinNo(c.getTinNo());
                    });
                }

                completedJobs.add(j);

                totalInvoiceAmount += invoiceTotal;
                totalExpensesAmount += expenses;
                totalProfitAmount += profit;
            }
        }

        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariable("jobs", completedJobs);
        context.setVariable("fromDate", from);
        context.setVariable("toDate", to);
        context.setVariable("totalInvoiceAmount", totalInvoiceAmount);
        context.setVariable("totalExpensesAmount", totalExpensesAmount);
        context.setVariable("totalProfitAmount", totalProfitAmount);

        try {
            byte[] pdfBytes = pdfService.generatePdf("jobs/report-profit-pdf", context);
            String filename = "ProfitReport_" + from + "_to_" + to + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename(filename).build());
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private double calculateJobExpenses(Job job) {
        double expenses = 0.0;
        if (job.getDoCharges() != null) expenses += job.getDoCharges();
        if (job.getCustomDutyAmount() != null) expenses += job.getCustomDutyAmount();
        if (job.getHipgCharges() != null) expenses += job.getHipgCharges();
        if (job.getCictCharges() != null) expenses += job.getCictCharges();
        if (job.getDoExtension() != null) expenses += job.getDoExtension();
        if (job.getValuationExpences() != null) expenses += job.getValuationExpences();
        if (job.getPlantQuarantine() != null) expenses += job.getPlantQuarantine();
        if (job.getRctCharges() != null) expenses += job.getRctCharges();
        if (job.getImportControlDebit() != null) expenses += job.getImportControlDebit();
        if (job.getSlpaBondingCharges() != null) expenses += job.getSlpaBondingCharges();
        if (job.getAmendmentCharges() != null) expenses += job.getAmendmentCharges();
        if (job.getHandlingExpenses() != null) expenses += job.getHandlingExpenses();
        if (job.getDocumentationExpences() != null) expenses += job.getDocumentationExpences();
        if (job.getExamination() != null) expenses += job.getExamination();
        if (job.getWeighBridgeCharges() != null) expenses += job.getWeighBridgeCharges();
        if (job.getLabour() != null) expenses += job.getLabour();
        if (job.getAdditional() != null) expenses += job.getAdditional();
        if (job.getVatRegistration() != null) expenses += job.getVatRegistration();
        if (job.getTradeRegistration() != null) expenses += job.getTradeRegistration();
        if (job.getTransport() != null) expenses += job.getTransport();
        if (job.getOther() != null) expenses += job.getOther();
        if (job.getAgencyFee() != null) expenses += job.getAgencyFee();
        if (job.getEntryPassing() != null) expenses += job.getEntryPassing();
        if (job.getDeliveryExpenses() != null) expenses += job.getDeliveryExpenses();
        if (job.getCommission() != null) expenses += job.getCommission();
        if (job.getCustomChargeValue() != null) expenses += job.getCustomChargeValue();
        return expenses;
    }

    // ── View Job Detail ──────────────────────────────────────────
    @GetMapping("/view/{id}")
    public String viewJob(@PathVariable String id, Model model, Authentication auth) {
        Optional<Job> job = jobService.getJobById(id);
        if (job.isEmpty()) return "redirect:/jobs";
        model.addAttribute("job", job.get());
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStaff", isStaff);
        model.addAttribute("username", auth != null ? auth.getName() : "User");
        return "jobs/view-job";
    }

    // ── Invoice Generate Form (Admin Only) ────────────────────────
    @GetMapping("/invoice/{id}")
    public String invoiceForm(@PathVariable String id, Model model, Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) return "redirect:/jobs";

        Optional<Job> jobOpt = jobService.getJobById(id);
        if (jobOpt.isEmpty()) return "redirect:/jobs";
        Job job = jobOpt.get();

        // If status is PENDING, transition to IN_PROGRESS since Admin is now working on invoicing
        if ("PENDING".equals(job.getStatus())) {
            job.setStatus("IN_PROGRESS");
            job.setUpdatedAt(LocalDateTime.now());
            jobService.saveJob(job);
        }

        // Pre-populate form with saved amounts from the job
        InvoiceGenerateRequest form = new InvoiceGenerateRequest();
        form.setDoCharges(job.getDoCharges());
        form.setCustomDutyAmount(job.getCustomDutyAmount());
        form.setHipgCharges(job.getHipgCharges());
        form.setCictCharges(job.getCictCharges());
        form.setDoExtension(job.getDoExtension());
        form.setValuationExpences(job.getValuationExpences());
        form.setPlantQuarantine(job.getPlantQuarantine());
        form.setRctCharges(job.getRctCharges());
        form.setImportControlDebit(job.getImportControlDebit());
        form.setSlpaBondingCharges(job.getSlpaBondingCharges());
        form.setAmendmentCharges(job.getAmendmentCharges());
        form.setHandlingExpenses(job.getHandlingExpenses());
        form.setDocumentationExpences(job.getDocumentationExpences());
        form.setExamination(job.getExamination());
        form.setWeighBridgeCharges(job.getWeighBridgeCharges());
        form.setLabour(job.getLabour());
        form.setAdditional(job.getAdditional());
        form.setVatRegistration(job.getVatRegistration());
        form.setTradeRegistration(job.getTradeRegistration());
        form.setTransport(job.getTransport());
        form.setOther(job.getOther());
        form.setAgencyFee(job.getAgencyFee());
        form.setBlAmount(job.getBlAmount());
        form.setAdvance(job.getAdvance());
        form.setBalance(job.getBalance());

        model.addAttribute("job", job);
        model.addAttribute("form", form);
        model.addAttribute("username", auth != null ? auth.getName() : "User");
        return "jobs/invoice-generate";
    }

    // ── Generate Invoice PDF — POST (Admin Only) ──────────────────
    @PostMapping("/invoice/{id}")
    public ResponseEntity<byte[]> generateInvoice(
            @PathVariable String id,
            @ModelAttribute InvoiceGenerateRequest form,
            Authentication auth) {

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Optional<Job> jobOpt = jobService.getJobById(id);
        if (jobOpt.isEmpty()) return ResponseEntity.notFound().build();
        Job job = jobOpt.get();

        // Save ONLY the totals, status and timestamp to MongoDB
        job.setBlAmount(orZero(form.getBlAmount()));
        job.setAdvance(orZero(form.getAdvance()));
        job.setBalance(orZero(form.getBalance()));
        job.setStatus("COMPLETED");
        job.setUpdatedAt(LocalDateTime.now());
        jobService.saveJob(job);

        // Load the Admin's edited charge values onto the job object in-memory (do not save to DB)
        // so they are available for PDF generation:
        job.setDoCharges(orZero(form.getDoCharges()));
        job.setCustomDutyAmount(orZero(form.getCustomDutyAmount()));
        job.setHipgCharges(orZero(form.getHipgCharges()));
        job.setCictCharges(orZero(form.getCictCharges()));
        job.setDoExtension(orZero(form.getDoExtension()));
        job.setValuationExpences(orZero(form.getValuationExpences()));
        job.setPlantQuarantine(orZero(form.getPlantQuarantine()));
        job.setRctCharges(orZero(form.getRctCharges()));
        job.setImportControlDebit(orZero(form.getImportControlDebit()));
        job.setSlpaBondingCharges(orZero(form.getSlpaBondingCharges()));
        job.setAmendmentCharges(orZero(form.getAmendmentCharges()));
        job.setHandlingExpenses(orZero(form.getHandlingExpenses()));
        job.setDocumentationExpences(orZero(form.getDocumentationExpences()));
        job.setExamination(orZero(form.getExamination()));
        job.setWeighBridgeCharges(orZero(form.getWeighBridgeCharges()));
        job.setLabour(orZero(form.getLabour()));
        job.setAdditional(orZero(form.getAdditional()));
        job.setVatRegistration(orZero(form.getVatRegistration()));
        job.setTradeRegistration(orZero(form.getTradeRegistration()));
        job.setTransport(orZero(form.getTransport()));
        job.setOther(orZero(form.getOther()));
        job.setAgencyFee(orZero(form.getAgencyFee()));
        job.setCustomChargeName(form.getCustomChargeName());
        job.setCustomChargeValue(orZero(form.getCustomChargeValue()));

        try {
            ClassPathResource imgResource = new ClassPathResource("static/idec-logo.png");
            byte[] imageBytes = StreamUtils.copyToByteArray(imgResource.getInputStream());
            String logoBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);

            // selectedCharges may be null/empty if no checkboxes were ticked
            Set<String> selected = form.getSelectedCharges() != null
                    ? form.getSelectedCharges()
                    : new HashSet<>();

            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
            context.setVariable("job", job);
            context.setVariable("logoBase64", logoBase64);
            context.setVariable("selectedCharges", selected);

            if (job.getCompanyId() != null && !job.getCompanyId().isBlank()) {
                companyService.getCompanyById(job.getCompanyId()).ifPresent(company -> {
                    context.setVariable("companyAddress", company.getAddress());
                });
            }

            byte[] pdfBytes = pdfService.generatePdf("jobs/invoice-pdf", context);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename("Invoice_" + job.getJobNo() + ".pdf")
                    .build());
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private Double orZero(Double val) {
        return val != null ? val : 0.0;
    }
}
