package com.finance.manager.config;

import com.finance.manager.entity.User;
import com.finance.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Create a default user for testing if it doesn't exist
        if (!userService.existsByUsername("user")) {
            userService.createUser(
                "user", 
                "user@example.com", 
                "password", 
                "Demo", 
                "User"
            );
            System.out.println("Created default user: username='user', password='password'");
        }
    }
}
