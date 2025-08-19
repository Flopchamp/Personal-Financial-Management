package com.finance.manager.controller;

import com.finance.manager.entity.Budget;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import com.finance.manager.service.BudgetService;
import com.finance.manager.service.CategoryService;
import com.finance.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/budgets")
public class BudgetController {
    
    @Autowired
    private BudgetService budgetService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String listBudgets(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        
        List<Budget> activeBudgets = budgetService.findByUserAndActive(user, true);
        List<Budget> inactiveBudgets = budgetService.findByUserAndActive(user, false);
        List<Budget> overBudgets = budgetService.getOverBudgets(user);
        
        // Calculate summary statistics
        BigDecimal totalBudgetAmount = budgetService.getTotalBudgetAmount(user);
        BigDecimal totalSpentAmount = budgetService.getTotalSpentAmount(user);
        long overBudgetCount = budgetService.getOverBudgetCount(user);
        
        model.addAttribute("activeBudgets", activeBudgets);
        model.addAttribute("inactiveBudgets", inactiveBudgets);
        model.addAttribute("overBudgets", overBudgets);
        model.addAttribute("totalBudgetAmount", totalBudgetAmount);
        model.addAttribute("totalSpentAmount", totalSpentAmount);
        model.addAttribute("overBudgetCount", overBudgetCount);
        
        return "budgets/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Category> categories = categoryService.findByUserAndType(user, Category.CategoryType.EXPENSE);
        
        Budget budget = new Budget();
        budget.setStartDate(LocalDate.now());
        budget.setEndDate(LocalDate.now().plusMonths(1));
        budget.setPeriod(Budget.BudgetPeriod.MONTHLY);
        
        model.addAttribute("budget", budget);
        model.addAttribute("categories", categories);
        model.addAttribute("budgetPeriods", Budget.BudgetPeriod.values());
        
        return "budgets/form";
    }
    
    @PostMapping
    public String createBudget(
            @Valid @ModelAttribute Budget budget,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(authentication);
        
        if (result.hasErrors()) {
            List<Category> categories = categoryService.findByUserAndType(user, Category.CategoryType.EXPENSE);
            model.addAttribute("categories", categories);
            model.addAttribute("budgetPeriods", Budget.BudgetPeriod.values());
            return "budgets/form";
        }
        
        // Validate date range
        if (budget.getEndDate().isBefore(budget.getStartDate())) {
            List<Category> categories = categoryService.findByUserAndType(user, Category.CategoryType.EXPENSE);
            model.addAttribute("categories", categories);
            model.addAttribute("budgetPeriods", Budget.BudgetPeriod.values());
            model.addAttribute("errorMessage", "End date must be after start date");
            return "budgets/form";
        }
        
        try {
            budgetService.createBudget(
                budget.getName(),
                budget.getAmount(),
                budget.getStartDate(),
                budget.getEndDate(),
                budget.getPeriod(),
                user,
                budget.getCategory(),
                budget.getDescription()
            );
            
            redirectAttributes.addFlashAttribute("successMessage", "Budget created successfully!");
            return "redirect:/budgets";
        } catch (Exception e) {
            List<Category> categories = categoryService.findByUserAndType(user, Category.CategoryType.EXPENSE);
            model.addAttribute("categories", categories);
            model.addAttribute("budgetPeriods", Budget.BudgetPeriod.values());
            model.addAttribute("errorMessage", "Error creating budget: " + e.getMessage());
            return "budgets/form";
        }
    }
    
    @GetMapping("/{id}")
    public String viewBudget(@PathVariable Long id, Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Budget> budget = budgetService.findById(id);
        
        if (budget.isEmpty() || !budget.get().getUser().getId().equals(user.getId())) {
            return "redirect:/budgets";
        }
        
        // Update spent amount to ensure accuracy
        budgetService.updateBudgetSpentAmount(budget.get());
        
        model.addAttribute("budget", budget.get());
        return "budgets/view";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Budget> budget = budgetService.findById(id);
        
        if (budget.isEmpty() || !budget.get().getUser().getId().equals(user.getId())) {
            return "redirect:/budgets";
        }
        
        List<Category> categories = categoryService.findByUserAndType(user, Category.CategoryType.EXPENSE);
        
        model.addAttribute("budget", budget.get());
        model.addAttribute("categories", categories);
        model.addAttribute("budgetPeriods", Budget.BudgetPeriod.values());
        
        return "budgets/form";
    }
    
    @PostMapping("/{id}")
    public String updateBudget(
            @PathVariable Long id,
            @Valid @ModelAttribute Budget budget,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(authentication);
        
        if (result.hasErrors()) {
            List<Category> categories = categoryService.findByUserAndType(user, Category.CategoryType.EXPENSE);
            model.addAttribute("categories", categories);
            model.addAttribute("budgetPeriods", Budget.BudgetPeriod.values());
            return "budgets/form";
        }
        
        // Validate date range
        if (budget.getEndDate().isBefore(budget.getStartDate())) {
            List<Category> categories = categoryService.findByUserAndType(user, Category.CategoryType.EXPENSE);
            model.addAttribute("categories", categories);
            model.addAttribute("budgetPeriods", Budget.BudgetPeriod.values());
            model.addAttribute("errorMessage", "End date must be after start date");
            return "budgets/form";
        }
        
        try {
            Optional<Budget> existingBudget = budgetService.findById(id);
            if (existingBudget.isEmpty() || !existingBudget.get().getUser().getId().equals(user.getId())) {
                return "redirect:/budgets";
            }
            
            budgetService.updateBudget(id, budget);
            redirectAttributes.addFlashAttribute("successMessage", "Budget updated successfully!");
            return "redirect:/budgets";
        } catch (Exception e) {
            List<Category> categories = categoryService.findByUserAndType(user, Category.CategoryType.EXPENSE);
            model.addAttribute("categories", categories);
            model.addAttribute("budgetPeriods", Budget.BudgetPeriod.values());
            model.addAttribute("errorMessage", "Error updating budget: " + e.getMessage());
            return "budgets/form";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteBudget(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(authentication);
        Optional<Budget> budget = budgetService.findById(id);
        
        if (budget.isEmpty() || !budget.get().getUser().getId().equals(user.getId())) {
            return "redirect:/budgets";
        }
        
        try {
            budgetService.deleteBudget(id);
            redirectAttributes.addFlashAttribute("successMessage", "Budget deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting budget: " + e.getMessage());
        }
        
        return "redirect:/budgets";
    }
    
    @PostMapping("/{id}/deactivate")
    public String deactivateBudget(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(authentication);
        Optional<Budget> budget = budgetService.findById(id);
        
        if (budget.isEmpty() || !budget.get().getUser().getId().equals(user.getId())) {
            return "redirect:/budgets";
        }
        
        try {
            budgetService.deactivateBudget(id);
            redirectAttributes.addFlashAttribute("successMessage", "Budget deactivated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deactivating budget: " + e.getMessage());
        }
        
        return "redirect:/budgets";
    }
    
    @PostMapping("/cleanup-expired")
    public String cleanupExpiredBudgets(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);
        
        try {
            budgetService.deactivateExpiredBudgets(user);
            redirectAttributes.addFlashAttribute("successMessage", "Expired budgets deactivated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cleaning up expired budgets: " + e.getMessage());
        }
        
        return "redirect:/budgets";
    }
    
    private User getCurrentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
