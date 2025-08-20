package com.finance.manager.controller;

import com.finance.manager.entity.User;
import com.finance.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user,
                              BindingResult bindingResult,
                              @RequestParam("confirmPassword") String confirmPassword,
                              @RequestParam(value = "terms", required = false) String terms,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        // Check if terms are accepted
        if (terms == null || !terms.equals("true")) {
            model.addAttribute("errorMessage", "You must accept the Terms of Service and Privacy Policy.");
            return "register";
        }
        
        // Check if passwords match
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "register";
        }

        // Check if username already exists
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("errorMessage", "Username already exists.");
            return "register";
        }

        // Check if email already exists
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("errorMessage", "Email already exists.");
            return "register";
        }

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Please correct the errors below.");
            return "register";
        }

        try {
            // Save user (password will be encoded in the service)
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registration successful! Please log in with your credentials.");
            return "redirect:/login";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed. Please try again.");
            return "register";
        }
    }
}
