package com.finance.manager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Column(unique = true)
    private String name;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private CategoryType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String color; // For UI display
    
    @Column(columnDefinition = "boolean default true")
    private boolean active = true;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Budget> budgets;
    
    // Enum for category types
    public enum CategoryType {
        INCOME, EXPENSE
    }
    
    // Constructors
    public Category() {}
    
    public Category(String name, String description, CategoryType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    
    public List<Budget> getBudgets() { return budgets; }
    public void setBudgets(List<Budget> budgets) { this.budgets = budgets; }
}
