package com.finance.manager.service;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private BudgetService budgetService;
    
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }
    
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }
    
    public List<Transaction> findByUser(User user) {
        return transactionRepository.findByUser(user);
    }
    
    public Page<Transaction> findByUser(User user, Pageable pageable) {
        return transactionRepository.findByUser(user, pageable);
    }
    
    public Page<Transaction> findByUserOrderByDateDesc(User user, Pageable pageable) {
        return transactionRepository.findByUserOrderByDateDesc(user, pageable);
    }
    
    public List<Transaction> findByUserAndCategory(User user, Category category) {
        return transactionRepository.findByUserAndCategory(user, category);
    }
    
    public List<Transaction> findByUserAndType(User user, Transaction.TransactionType type) {
        return transactionRepository.findByUserAndType(user, type);
    }
    
    public List<Transaction> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserAndDateBetween(user, startDate, endDate);
    }
    
    public Page<Transaction> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return transactionRepository.findByUserAndDateBetween(user, startDate, endDate, pageable);
    }
    
    public List<Transaction> findByUserAndCategoryAndDateBetween(User user, Category category, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserAndCategoryAndDateBetween(user, category, startDate, endDate);
    }
    
    public List<Transaction> searchTransactions(User user, String keyword) {
        return transactionRepository.findByUserAndDescriptionOrNotesContaining(user, keyword);
    }
    
    public Transaction save(Transaction transaction) {
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Update budget spent amounts if this is an expense
        if (savedTransaction.getType() == Transaction.TransactionType.EXPENSE) {
            budgetService.updateBudgetSpentAmounts(savedTransaction.getUser(), savedTransaction.getCategory(), savedTransaction.getDate());
        }
        
        return savedTransaction;
    }
    
    public Transaction createTransaction(String description, BigDecimal amount, LocalDate date,
                                       Transaction.TransactionType type, User user, Category category, String notes) {
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setType(type);
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setNotes(notes);
        
        return save(transaction);
    }
    
    public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
        return transactionRepository.findById(id)
                .map(transaction -> {
                    User originalUser = transaction.getUser();
                    Category originalCategory = transaction.getCategory();
                    LocalDate originalDate = transaction.getDate();
                    Transaction.TransactionType originalType = transaction.getType();
                    
                    transaction.setDescription(updatedTransaction.getDescription());
                    transaction.setAmount(updatedTransaction.getAmount());
                    transaction.setDate(updatedTransaction.getDate());
                    transaction.setType(updatedTransaction.getType());
                    transaction.setCategory(updatedTransaction.getCategory());
                    transaction.setNotes(updatedTransaction.getNotes());
                    
                    Transaction saved = transactionRepository.save(transaction);
                    
                    // Update budget amounts for both old and new categories/dates if this affects expenses
                    if (originalType == Transaction.TransactionType.EXPENSE || saved.getType() == Transaction.TransactionType.EXPENSE) {
                        budgetService.updateBudgetSpentAmounts(originalUser, originalCategory, originalDate);
                        budgetService.updateBudgetSpentAmounts(saved.getUser(), saved.getCategory(), saved.getDate());
                    }
                    
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }
    
    public void deleteTransaction(Long id) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            transactionRepository.deleteById(id);
            
            // Update budget spent amounts if this was an expense
            if (transaction.getType() == Transaction.TransactionType.EXPENSE) {
                budgetService.updateBudgetSpentAmounts(transaction.getUser(), transaction.getCategory(), transaction.getDate());
            }
        }
    }
    
    // Financial calculation methods
    public BigDecimal getTotalIncomeByUser(User user) {
        BigDecimal total = transactionRepository.sumAmountByUserAndType(user, Transaction.TransactionType.INCOME);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalExpenseByUser(User user) {
        BigDecimal total = transactionRepository.sumAmountByUserAndType(user, Transaction.TransactionType.EXPENSE);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public BigDecimal getNetIncomeByUser(User user) {
        return getTotalIncomeByUser(user).subtract(getTotalExpenseByUser(user));
    }
    
    public BigDecimal getTotalIncomeByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.sumAmountByUserAndTypeAndDateBetween(user, Transaction.TransactionType.INCOME, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalExpenseByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.sumAmountByUserAndTypeAndDateBetween(user, Transaction.TransactionType.EXPENSE, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public BigDecimal getNetIncomeByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return getTotalIncomeByUserAndDateRange(user, startDate, endDate)
                .subtract(getTotalExpenseByUserAndDateRange(user, startDate, endDate));
    }
    
    public BigDecimal getTotalExpenseByCategoryAndDateRange(User user, Category category, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.sumAmountByUserAndCategoryAndDateBetween(user, category, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public long getTransactionCountByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.countByUserAndDateBetween(user, startDate, endDate);
    }
}
