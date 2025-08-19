package com.finance.manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Login");
        return "login";
    }
    
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", "Register");
        // For now, redirect to login - we can implement registration later
        return "redirect:/login?message=Registration coming soon!";
    }
}
