package com.finance.manager.controller;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.service.CategoryService;
import com.finance.manager.service.TransactionService;
import com.finance.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/transactions")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Transaction.TransactionType type,
            Authentication authentication,
            Model model) {
        
        User user = getCurrentUser(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions;
        
        // Apply filters based on parameters
        if (search != null && !search.trim().isEmpty()) {
            List<Transaction> searchResults = transactionService.searchTransactions(user, search.trim());
            // Convert list to page for consistency (simplified pagination)
            transactions = transactionService.findByUserOrderByDateDesc(user, pageable);
        } else if (startDate != null && endDate != null) {
            transactions = transactionService.findByUserAndDateBetween(user, startDate, endDate, pageable);
        } else {
            transactions = transactionService.findByUserOrderByDateDesc(user, pageable);
        }
        
        // Get categories for filter dropdown
        List<Category> categories = categoryService.findByUserOrderByTypeAndName(user);
        
        // Calculate financial summary
        BigDecimal totalIncome = transactionService.getTotalIncomeByUser(user);
        BigDecimal totalExpense = transactionService.getTotalExpenseByUser(user);
        BigDecimal netIncome = totalIncome.subtract(totalExpense);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("categories", categories);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("netIncome", netIncome);
        model.addAttribute("search", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedType", type);
        
        return "transactions/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Category> categories = categoryService.findByUserOrderByTypeAndName(user);
        
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("categories", categories);
        
        return "transactions/form";
    }
    
    @PostMapping
    public String createTransaction(
            @Valid @ModelAttribute Transaction transaction,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(authentication);
        
        if (result.hasErrors()) {
            List<Category> categories = categoryService.findByUserOrderByTypeAndName(user);
            model.addAttribute("categories", categories);
            return "transactions/form";
        }
        
        try {
            transactionService.createTransaction(
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getType(),
                user,
                transaction.getCategory(),
                transaction.getNotes()
            );
            
            redirectAttributes.addFlashAttribute("successMessage", "Transaction created successfully!");
            return "redirect:/transactions";
        } catch (Exception e) {
            List<Category> categories = categoryService.findByUserOrderByTypeAndName(user);
            model.addAttribute("categories", categories);
            model.addAttribute("errorMessage", "Error creating transaction: " + e.getMessage());
            return "transactions/form";
        }
    }
    
    @GetMapping("/{id}")
    public String viewTransaction(@PathVariable Long id, Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Transaction> transaction = transactionService.findById(id);
        
        if (transaction.isEmpty() || !transaction.get().getUser().getId().equals(user.getId())) {
            return "redirect:/transactions";
        }
        
        model.addAttribute("transaction", transaction.get());
        return "transactions/view";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Transaction> transaction = transactionService.findById(id);
        
        if (transaction.isEmpty() || !transaction.get().getUser().getId().equals(user.getId())) {
            return "redirect:/transactions";
        }
        
        List<Category> categories = categoryService.findByUserOrderByTypeAndName(user);
        
        model.addAttribute("transaction", transaction.get());
        model.addAttribute("categories", categories);
        
        return "transactions/form";
    }
    
    @PostMapping("/{id}")
    public String updateTransaction(
            @PathVariable Long id,
            @Valid @ModelAttribute Transaction transaction,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(authentication);
        
        if (result.hasErrors()) {
            List<Category> categories = categoryService.findByUserOrderByTypeAndName(user);
            model.addAttribute("categories", categories);
            return "transactions/form";
        }
        
        try {
            Optional<Transaction> existingTransaction = transactionService.findById(id);
            if (existingTransaction.isEmpty() || !existingTransaction.get().getUser().getId().equals(user.getId())) {
                return "redirect:/transactions";
            }
            
            transactionService.updateTransaction(id, transaction);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction updated successfully!");
            return "redirect:/transactions";
        } catch (Exception e) {
            List<Category> categories = categoryService.findByUserOrderByTypeAndName(user);
            model.addAttribute("categories", categories);
            model.addAttribute("errorMessage", "Error updating transaction: " + e.getMessage());
            return "transactions/form";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteTransaction(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(authentication);
        Optional<Transaction> transaction = transactionService.findById(id);
        
        if (transaction.isEmpty() || !transaction.get().getUser().getId().equals(user.getId())) {
            return "redirect:/transactions";
        }
        
        try {
            transactionService.deleteTransaction(id);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting transaction: " + e.getMessage());
        }
        
        return "redirect:/transactions";
    }
    
    @GetMapping("/summary")
    public String showSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication,
            Model model) {
        
        User user = getCurrentUser(authentication);
        
        // Set default date range if not provided (current month)
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        // Get financial summary for date range
        BigDecimal totalIncome = transactionService.getTotalIncomeByUserAndDateRange(user, startDate, endDate);
        BigDecimal totalExpense = transactionService.getTotalExpenseByUserAndDateRange(user, startDate, endDate);
        BigDecimal netIncome = totalIncome.subtract(totalExpense);
        long transactionCount = transactionService.getTransactionCountByUserAndDateRange(user, startDate, endDate);
        
        // Get transactions by category for chart data
        List<Category> categories = categoryService.findByUserOrderByTypeAndName(user);
        
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("netIncome", netIncome);
        model.addAttribute("transactionCount", transactionCount);
        model.addAttribute("categories", categories);
        
        return "transactions/summary";
    }
    
    private User getCurrentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
