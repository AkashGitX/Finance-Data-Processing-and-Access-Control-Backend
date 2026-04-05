package com.akashzorvyn.zorvynproj.config;

import com.akashzorvyn.zorvynproj.entity.Role;
import com.akashzorvyn.zorvynproj.entity.User;
import com.akashzorvyn.zorvynproj.entity.UserStatus;
import com.akashzorvyn.zorvynproj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@financeapp.com")) {
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@financeapp.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
            log.info("=======================================================");
            log.info("Default ADMIN user created:");
            log.info("  Email:    admin@financeapp.com");
            log.info("  Password: Admin@123");
            log.info("  Role:     ADMIN");
            log.info("  CHANGE THE PASSWORD AFTER FIRST LOGIN!");
            log.info("=======================================================");
        } else {
            log.info("Admin user already exists. Skipping seed.");
        }
    }
}
