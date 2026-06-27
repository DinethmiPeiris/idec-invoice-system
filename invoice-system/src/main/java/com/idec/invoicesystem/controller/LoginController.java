package com.idec.invoicesystem.controller;

import com.idec.invoicesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles navigation to the login page.
 * Spring Security handles the actual POST /login authentication.
 */
@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Show the login page.
     *
     * @param error   present when authentication failed
     * @param logout  present when the user just logged out
     * @param model   Thymeleaf model
     * @return login template name
     */
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error",  required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        boolean isDb = "db".equals(error);
        model.addAttribute("isDbError", isDb);

        if (error != null) {
            if (isDb) {
                model.addAttribute("error", "Database connection timed out or failed. Your IP address might not be whitelisted in MongoDB Atlas.");
            } else {
                model.addAttribute("error", "Invalid username or password. Please try again.");
            }
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        return "login";   // resolves to templates/login.html
    }

    /**
     * Endpoint to check database status asynchronously.
     */
    @GetMapping("/api/db-status")
    @ResponseBody
    public Map<String, Object> checkDbStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            // Lightweight call to verify connection
            userRepository.count();
            status.put("status", "UP");
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
        }
        return status;
    }

    /**
     * Redirect root URL to login page (or dashboard once secured).
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }
}

