package com.idec.invoicesystem.controller;

import com.idec.invoicesystem.model.Company;
import com.idec.invoicesystem.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/companies")
public class CompanyController {

    @Autowired private CompanyService companyService;

    @GetMapping
    public String allCompanies(Model model, Authentication auth) {
        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("newCompany", new Company());
        model.addAttribute("username", auth != null ? auth.getName() : "User");
        return "companies/companies";
    }

    @PostMapping("/add")
    public String addCompany(@ModelAttribute Company company, RedirectAttributes ra) {
        if (companyService.existsByName(company.getName())) {
            ra.addFlashAttribute("error", "Company '" + company.getName() + "' already exists.");
        } else {
            companyService.saveCompany(company);
            ra.addFlashAttribute("success", "Company added successfully.");
        }
        return "redirect:/companies";
    }

    @PostMapping("/delete/{id}")
    public String deleteCompany(@PathVariable String id, RedirectAttributes ra) {
        companyService.deleteCompany(id);
        ra.addFlashAttribute("success", "Company removed.");
        return "redirect:/companies";
    }
}
