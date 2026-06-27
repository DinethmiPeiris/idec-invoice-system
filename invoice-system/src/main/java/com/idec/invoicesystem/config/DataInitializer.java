package com.idec.invoicesystem.config;

import com.idec.invoicesystem.model.User;
import com.idec.invoicesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default user accounts in MongoDB on application startup if the database has no users.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            if (userRepository.count() == 0) {
                // Create Default Admin
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("idec2024"));
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);

                // Create Default Staff
                User staff = new User();
                staff.setUsername("staff");
                staff.setPassword(passwordEncoder.encode("staff123"));
                staff.setRole("ROLE_STAFF");
                userRepository.save(staff);

                System.out.println(">>> Database initialized with default user accounts: admin/idec2024, staff/staff123");
            }
        } catch (Exception e) {
            System.err.println(">>> Failed to seed database: " + e.getMessage());
        }
    }
}
