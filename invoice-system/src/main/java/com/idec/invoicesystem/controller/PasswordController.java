package com.idec.invoicesystem.controller;

import com.idec.invoicesystem.model.User;
import com.idec.invoicesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller to handle password changes for authenticated users.
 */
@Controller
public class PasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/change-password")
    public String changePasswordForm(Model model, Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return "redirect:/jobs";
        }
        model.addAttribute("activePage", "change-password");
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        if (auth == null) {
            return "redirect:/login";
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return "redirect:/jobs";
        }

        String username = auth.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("error", "User session not found.");
            return "redirect:/change-password";
        }

        User user = userOpt.get();

        // 1. Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            ra.addFlashAttribute("error", "Incorrect current password. Please try again.");
            return "redirect:/change-password";
        }

        // 2. Validate new password strength
        if (newPassword == null || newPassword.trim().isEmpty()) {
            ra.addFlashAttribute("error", "New password cannot be empty.");
            return "redirect:/change-password";
        }

        // 3. Verify new passwords match
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/change-password";
        }

        // 4. Check if new password is same as old
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            ra.addFlashAttribute("error", "New password cannot be the same as your current password.");
            return "redirect:/change-password";
        }

        // 5. Save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        ra.addFlashAttribute("success", "Password updated successfully!");
        return "redirect:/change-password";
    }
}
