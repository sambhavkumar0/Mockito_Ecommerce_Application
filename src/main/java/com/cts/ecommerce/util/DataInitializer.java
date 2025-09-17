package com.cts.ecommerce.util;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.cts.ecommerce.model.Role;
import com.cts.ecommerce.model.User;
import com.cts.ecommerce.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("adminpass")); // change later
            admin.setEmail("admin@example.com");
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setPhoneNumber("+1234567890");
            admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
            admin.setActive(true); // Ensure admin is active
            userRepository.save(admin);
            System.out.println("✅ Default admin user created: username=admin, password=adminpass");
        }
        else {
        	System.out.println("The Admin User is already created and present in the database!!!!!");
        	System.out.println("email : 'admin@example.com', pass:'adminpass'");
        }
    }
}
