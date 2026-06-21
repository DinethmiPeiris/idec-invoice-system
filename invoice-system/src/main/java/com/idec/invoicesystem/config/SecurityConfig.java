package com.idec.invoicesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * Spring Security configuration for the IDEC Invoice System.
 *
 * NOTE: The in-memory user store here is for initial development only.
 * Replace with a MongoDB-backed UserDetailsService once the User model is ready.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Security will automatically wire and use our MongoUserDetailsService bean
    // since we do not define a custom UserDetailsService bean here anymore.

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login", "/api/db-status", "/css/**", "/js/**", "/images/**",
                    "/*.png", "/*.ico", "/error"
                ).permitAll()
                .requestMatchers("/dashboard/**", "/jobs/**", "/companies/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")            // our custom login page
                .loginProcessingUrl("/login")   // Spring Security handles POST here
                .defaultSuccessUrl("/dashboard", true)
                .failureHandler(authenticationFailureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            System.err.println("Authentication failed: " + exception.getMessage());
            Throwable cause = exception.getCause();
            if (cause != null) {
                System.err.println("Authentication failure cause: " + cause.toString());
            }

            boolean isDbError = false;
            Throwable t = exception;
            while (t != null) {
                String name = t.getClass().getName();
                if (name.contains("Mongo") || name.contains("DataAccess") || name.contains("Connection") || name.contains("Timeout")) {
                    isDbError = true;
                    break;
                }
                if (t.getMessage() != null && (t.getMessage().contains("Mongo") || t.getMessage().contains("connection") || t.getMessage().contains("Timeout"))) {
                    isDbError = true;
                    break;
                }
                t = t.getCause();
            }

            if (isDbError) {
                response.sendRedirect("/login?error=db");
            } else {
                response.sendRedirect("/login?error=true");
            }
        };
    }
}

