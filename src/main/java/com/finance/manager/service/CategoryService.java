package com.finance.manager.service;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import com.finance.manager.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
    
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }
    
    public List<Category> findByUser(User user) {
        return categoryRepository.findByUser(user);
    }
    
    public List<Category> findByUserOrderByTypeAndName(User user) {
        return categoryRepository.findByUserOrderByTypeAndName(user);
    }
    
    public List<Category> findByUserAndType(User user, Category.CategoryType type) {
        return categoryRepository.findByUserAndType(user, type);
    }
    
    public List<Category> findByUserAndTypeOrderByName(User user, Category.CategoryType type) {
        return categoryRepository.findByUserAndTypeOrderByName(user, type);
    }
    
    public Optional<Category> findByUserAndName(User user, String name) {
        return categoryRepository.findByUserAndName(user, name);
    }
    
    public Category save(Category category) {
        return categoryRepository.save(category);
    }
    
    public Category createCategory(String name, String description, Category.CategoryType type, User user) {
        if (existsByUserAndName(user, name)) {
            throw new RuntimeException("Category with name '" + name + "' already exists for this user");
        }
        
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setType(type);
        category.setUser(user);
        
        return save(category);
    }
    
    public Category updateCategory(Long id, Category updatedCategory) {
        return categoryRepository.findById(id)
                .map(category -> {
                    // Check if name is being changed and if new name already exists
                    if (!category.getName().equals(updatedCategory.getName())) {
                        if (existsByUserAndNameExcludingId(category.getUser(), updatedCategory.getName(), id)) {
                            throw new RuntimeException("Category with name '" + updatedCategory.getName() + "' already exists");
                        }
                    }
                    
                    category.setName(updatedCategory.getName());
                    category.setDescription(updatedCategory.getDescription());
                    category.setType(updatedCategory.getType());
                    
                    return save(category);
                })
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }
    
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
    
    public boolean existsByUserAndName(User user, String name) {
        return categoryRepository.existsByUserAndName(user, name);
    }
    
    public boolean existsByUserAndNameExcludingId(User user, String name, Long excludeId) {
        return categoryRepository.existsByUserAndNameExcludingId(user, name, excludeId);
    }
    
    public long countByUserAndType(User user, Category.CategoryType type) {
        return categoryRepository.countByUserAndType(user, type);
    }
    
    public void createDefaultCategories(User user) {
        // Create default income categories
        if (countByUserAndType(user, Category.CategoryType.INCOME) == 0) {
            createCategory("Salary", "Regular salary income", Category.CategoryType.INCOME, user);
            createCategory("Freelance", "Freelance work income", Category.CategoryType.INCOME, user);
            createCategory("Investment", "Investment returns", Category.CategoryType.INCOME, user);
            createCategory("Other Income", "Other sources of income", Category.CategoryType.INCOME, user);
        }
        
        // Create default expense categories
        if (countByUserAndType(user, Category.CategoryType.EXPENSE) == 0) {
            createCategory("Food & Dining", "Food and restaurant expenses", Category.CategoryType.EXPENSE, user);
            createCategory("Transportation", "Transportation costs", Category.CategoryType.EXPENSE, user);
            createCategory("Housing", "Rent, utilities, and housing costs", Category.CategoryType.EXPENSE, user);
            createCategory("Entertainment", "Entertainment and leisure", Category.CategoryType.EXPENSE, user);
            createCategory("Healthcare", "Medical and healthcare expenses", Category.CategoryType.EXPENSE, user);
            createCategory("Shopping", "Shopping and personal items", Category.CategoryType.EXPENSE, user);
            createCategory("Bills & Utilities", "Bills and utility payments", Category.CategoryType.EXPENSE, user);
            createCategory("Other Expenses", "Other miscellaneous expenses", Category.CategoryType.EXPENSE, user);
        }
    }
}
