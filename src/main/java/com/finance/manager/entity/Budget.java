package com.finance.manager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets")
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Budget name is required")
    @Size(max = 100, message = "Budget name must not exceed 100 characters")
    private String name;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Period is required")
    private BudgetPeriod period;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;
    
    private boolean active = true;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Enum for budget periods
    public enum BudgetPeriod {
        WEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM
    }
    
    // Constructors
    public Budget() {}
    
    public Budget(String name, BigDecimal amount, LocalDate startDate, LocalDate endDate,
                  BudgetPeriod period, User user, Category category) {
        this.name = name;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.period = period;
        this.user = user;
        this.category = category;
    }
    
    // Business methods
    public BigDecimal getRemainingAmount() {
        return amount.subtract(spentAmount);
    }
    
    public double getSpentPercentage() {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return spentAmount.divide(amount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }
    
    public boolean isOverBudget() {
        return spentAmount.compareTo(amount) > 0;
    }
    
    public boolean isWithinPeriod(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public BudgetPeriod getPeriod() { return period; }
    public void setPeriod(BudgetPeriod period) { this.period = period; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
