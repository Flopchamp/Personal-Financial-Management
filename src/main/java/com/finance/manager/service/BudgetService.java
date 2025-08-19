package com.finance.manager.service;

import com.finance.manager.entity.Budget;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import com.finance.manager.repository.BudgetRepository;
import com.finance.manager.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BudgetService {
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }
    
    public Optional<Budget> findById(Long id) {
        return budgetRepository.findById(id);
    }
    
    public List<Budget> findByUser(User user) {
        return budgetRepository.findByUser(user);
    }
    
    public List<Budget> findByUserOrderByEndDate(User user) {
        return budgetRepository.findByUserOrderByEndDate(user);
    }
    
    public List<Budget> findByUserAndActive(User user, boolean active) {
        return budgetRepository.findByUserAndActive(user, active);
    }
    
    public List<Budget> findActiveByUserAndDate(User user, LocalDate date) {
        return budgetRepository.findActiveByUserAndDate(user, date);
    }
    
    public List<Budget> findActiveByCategoryAndDate(User user, Category category, LocalDate date) {
        return budgetRepository.findActiveByCategoryAndDate(user, category, date);
    }
    
    public List<Budget> findExpiredBudgets(User user) {
        return budgetRepository.findExpiredBudgets(user, LocalDate.now());
    }
    
    public Budget save(Budget budget) {
        Budget savedBudget = budgetRepository.save(budget);
        // Recalculate spent amount after saving
        updateBudgetSpentAmount(savedBudget);
        return savedBudget;
    }
    
    public Budget createBudget(String name, BigDecimal amount, LocalDate startDate, LocalDate endDate,
                              Budget.BudgetPeriod period, User user, Category category, String description) {
        if (budgetRepository.existsByUserAndNameAndActive(user, name, true)) {
            throw new RuntimeException("Active budget with name '" + name + "' already exists");
        }
        
        Budget budget = new Budget();
        budget.setName(name);
        budget.setAmount(amount);
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);
        budget.setPeriod(period);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setDescription(description);
        budget.setActive(true);
        
        return save(budget);
    }
    
    public Budget updateBudget(Long id, Budget updatedBudget) {
        return budgetRepository.findById(id)
                .map(budget -> {
                    budget.setName(updatedBudget.getName());
                    budget.setAmount(updatedBudget.getAmount());
                    budget.setStartDate(updatedBudget.getStartDate());
                    budget.setEndDate(updatedBudget.getEndDate());
                    budget.setPeriod(updatedBudget.getPeriod());
                    budget.setCategory(updatedBudget.getCategory());
                    budget.setDescription(updatedBudget.getDescription());
                    budget.setActive(updatedBudget.isActive());
                    
                    return save(budget);
                })
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
    }
    
    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }
    
    public void deactivateBudget(Long id) {
        budgetRepository.findById(id)
                .ifPresent(budget -> {
                    budget.setActive(false);
                    budgetRepository.save(budget);
                });
    }
    
    public void updateBudgetSpentAmount(Budget budget) {
        BigDecimal spentAmount = transactionRepository.sumAmountByUserAndCategoryAndDateBetween(
                budget.getUser(), budget.getCategory(), budget.getStartDate(), budget.getEndDate());
        
        budget.setSpentAmount(spentAmount != null ? spentAmount : BigDecimal.ZERO);
        budgetRepository.save(budget);
    }
    
    public void updateBudgetSpentAmounts(User user, Category category, LocalDate date) {
        List<Budget> affectedBudgets = findActiveByCategoryAndDate(user, category, date);
        for (Budget budget : affectedBudgets) {
            updateBudgetSpentAmount(budget);
        }
    }
    
    public void updateAllActiveBudgetSpentAmounts(User user) {
        List<Budget> activeBudgets = findByUserAndActive(user, true);
        for (Budget budget : activeBudgets) {
            updateBudgetSpentAmount(budget);
        }
    }
    
    // Budget analysis methods
    public List<Budget> getOverBudgets(User user) {
        return findByUserAndActive(user, true).stream()
                .filter(Budget::isOverBudget)
                .toList();
    }
    
    public BigDecimal getTotalBudgetAmount(User user) {
        BigDecimal total = budgetRepository.sumActivebudgetAmountByUser(user);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalSpentAmount(User user) {
        BigDecimal total = budgetRepository.sumSpentAmountByUser(user);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public long getOverBudgetCount(User user) {
        return budgetRepository.countOverBudgetsByUser(user);
    }
    
    public void deactivateExpiredBudgets(User user) {
        List<Budget> expiredBudgets = findExpiredBudgets(user);
        for (Budget budget : expiredBudgets) {
            budget.setActive(false);
            budgetRepository.save(budget);
        }
    }
    
    public Budget.BudgetPeriod determinePeriodFromDates(LocalDate startDate, LocalDate endDate) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        
        if (daysBetween <= 7) {
            return Budget.BudgetPeriod.WEEKLY;
        } else if (daysBetween <= 31) {
            return Budget.BudgetPeriod.MONTHLY;
        } else if (daysBetween <= 93) {
            return Budget.BudgetPeriod.QUARTERLY;
        } else if (daysBetween <= 366) {
            return Budget.BudgetPeriod.YEARLY;
        } else {
            return Budget.BudgetPeriod.CUSTOM;
        }
    }
}
