package com.finance.manager.controller;

import com.finance.manager.entity.Budget;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.service.BudgetService;
import com.finance.manager.service.CategoryService;
import com.finance.manager.service.TransactionService;
import com.finance.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private BudgetService budgetService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        model.addAttribute("title", "Personal Finance Manager");
        
        // If user is authenticated, redirect to dashboard
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        
        return "index";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user = getCurrentUser(authentication);
        
        // Current month date range
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now();
        
        // Financial summary for current month
        BigDecimal monthlyIncome = transactionService.getTotalIncomeByUserAndDateRange(user, startOfMonth, endOfMonth);
        BigDecimal monthlyExpense = transactionService.getTotalExpenseByUserAndDateRange(user, startOfMonth, endOfMonth);
        BigDecimal monthlyNet = monthlyIncome.subtract(monthlyExpense);
        
        // Overall financial summary
        BigDecimal totalIncome = transactionService.getTotalIncomeByUser(user);
        BigDecimal totalExpense = transactionService.getTotalExpenseByUser(user);
        BigDecimal totalNet = totalIncome.subtract(totalExpense);
        
        // Recent transactions (last 5)
        Pageable recentTransactionsPageable = PageRequest.of(0, 5);
        List<Transaction> recentTransactions = transactionService.findByUserOrderByDateDesc(user, recentTransactionsPageable).getContent();
        
        // Active budgets
        List<Budget> activeBudgets = budgetService.findByUserAndActive(user, true);
        List<Budget> overBudgets = budgetService.getOverBudgets(user);
        
        // Budget summary
        BigDecimal totalBudgetAmount = budgetService.getTotalBudgetAmount(user);
        BigDecimal totalSpentAmount = budgetService.getTotalSpentAmount(user);
        long overBudgetCount = budgetService.getOverBudgetCount(user);
        
        // Transaction count for current month
        long monthlyTransactionCount = transactionService.getTransactionCountByUserAndDateRange(user, startOfMonth, endOfMonth);
        
        // Check if user has default categories
        long incomeCategories = categoryService.countByUserAndType(user, com.finance.manager.entity.Category.CategoryType.INCOME);
        long expenseCategories = categoryService.countByUserAndType(user, com.finance.manager.entity.Category.CategoryType.EXPENSE);
        boolean needsDefaultCategories = (incomeCategories == 0 || expenseCategories == 0);
        
        // Generate chart data for the last 6 months
        Map<String, Object> chartData = generateChartData(user, 6);
        
        model.addAttribute("title", "Dashboard");
        model.addAttribute("user", user);
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("monthlyExpense", monthlyExpense);
        model.addAttribute("monthlyNet", monthlyNet);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("totalNet", totalNet);
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("activeBudgets", activeBudgets);
        model.addAttribute("overBudgets", overBudgets);
        model.addAttribute("totalBudgetAmount", totalBudgetAmount);
        model.addAttribute("totalSpentAmount", totalSpentAmount);
        model.addAttribute("overBudgetCount", overBudgetCount);
        model.addAttribute("monthlyTransactionCount", monthlyTransactionCount);
        model.addAttribute("needsDefaultCategories", needsDefaultCategories);
        model.addAttribute("currentMonth", LocalDate.now().getMonth().toString());
        model.addAttribute("chartData", chartData);
        
        return "dashboard";
    }
    
    private Map<String, Object> generateChartData(User user, int months) {
        Map<String, Object> data = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> incomeData = new ArrayList<>();
        List<BigDecimal> expenseData = new ArrayList<>();
        List<BigDecimal> netData = new ArrayList<>();
        
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
        // Generate data for the last 'months' months
        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = currentDate.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            
            BigDecimal monthlyIncome = transactionService.getTotalIncomeByUserAndDateRange(user, monthStart, monthEnd);
            BigDecimal monthlyExpense = transactionService.getTotalExpenseByUserAndDateRange(user, monthStart, monthEnd);
            BigDecimal monthlyNet = monthlyIncome.subtract(monthlyExpense);
            
            labels.add(monthStart.format(formatter));
            incomeData.add(monthlyIncome);
            expenseData.add(monthlyExpense);
            netData.add(monthlyNet);
        }
        
        data.put("labels", labels);
        data.put("incomeData", incomeData);
        data.put("expenseData", expenseData);
        data.put("netData", netData);
        
        return data;
    }
    
    private User getCurrentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
