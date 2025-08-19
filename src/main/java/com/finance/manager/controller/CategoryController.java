package com.finance.manager.controller;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import com.finance.manager.service.CategoryService;
import com.finance.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String listCategories(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        
        List<Category> incomeCategories = categoryService.findByUserAndType(user, Category.CategoryType.INCOME);
        List<Category> expenseCategories = categoryService.findByUserAndType(user, Category.CategoryType.EXPENSE);
        
        model.addAttribute("incomeCategories", incomeCategories);
        model.addAttribute("expenseCategories", expenseCategories);
        
        return "categories/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("categoryTypes", Category.CategoryType.values());
        return "categories/form";
    }
    
    @PostMapping
    public String createCategory(
            @Valid @ModelAttribute Category category,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(authentication);
        
        if (result.hasErrors()) {
            model.addAttribute("categoryTypes", Category.CategoryType.values());
            return "categories/form";
        }
        
        try {
            categoryService.createCategory(
                category.getName(),
                category.getDescription(),
                category.getType(),
                user
            );
            
            redirectAttributes.addFlashAttribute("successMessage", "Category created successfully!");
            return "redirect:/categories";
        } catch (Exception e) {
            model.addAttribute("categoryTypes", Category.CategoryType.values());
            model.addAttribute("errorMessage", "Error creating category: " + e.getMessage());
            return "categories/form";
        }
    }
    
    @GetMapping("/{id}")
    public String viewCategory(@PathVariable Long id, Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Category> category = categoryService.findById(id);
        
        if (category.isEmpty() || !category.get().getUser().getId().equals(user.getId())) {
            return "redirect:/categories";
        }
        
        model.addAttribute("category", category.get());
        return "categories/view";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Category> category = categoryService.findById(id);
        
        if (category.isEmpty() || !category.get().getUser().getId().equals(user.getId())) {
            return "redirect:/categories";
        }
        
        model.addAttribute("category", category.get());
        model.addAttribute("categoryTypes", Category.CategoryType.values());
        
        return "categories/form";
    }
    
    @PostMapping("/{id}")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute Category category,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(authentication);
        
        if (result.hasErrors()) {
            model.addAttribute("categoryTypes", Category.CategoryType.values());
            return "categories/form";
        }
        
        try {
            Optional<Category> existingCategory = categoryService.findById(id);
            if (existingCategory.isEmpty() || !existingCategory.get().getUser().getId().equals(user.getId())) {
                return "redirect:/categories";
            }
            
            categoryService.updateCategory(id, category);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
            return "redirect:/categories";
        } catch (Exception e) {
            model.addAttribute("categoryTypes", Category.CategoryType.values());
            model.addAttribute("errorMessage", "Error updating category: " + e.getMessage());
            return "categories/form";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteCategory(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(authentication);
        Optional<Category> category = categoryService.findById(id);
        
        if (category.isEmpty() || !category.get().getUser().getId().equals(user.getId())) {
            return "redirect:/categories";
        }
        
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting category: " + e.getMessage());
        }
        
        return "redirect:/categories";
    }
    
    @PostMapping("/create-defaults")
    public String createDefaultCategories(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);
        
        try {
            categoryService.createDefaultCategories(user);
            redirectAttributes.addFlashAttribute("successMessage", "Default categories created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating default categories: " + e.getMessage());
        }
        
        return "redirect:/categories";
    }
    
    private User getCurrentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
