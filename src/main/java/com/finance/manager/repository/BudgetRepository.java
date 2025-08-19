package com.finance.manager.repository;

import com.finance.manager.entity.Budget;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    List<Budget> findByUser(User user);
    
    List<Budget> findByUserAndActive(User user, boolean active);
    
    List<Budget> findByUserAndCategory(User user, Category category);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.startDate <= :date AND b.endDate >= :date")
    List<Budget> findActiveByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.category = :category AND b.startDate <= :date AND b.endDate >= :date AND b.active = true")
    List<Budget> findActiveByCategoryAndDate(@Param("user") User user, 
                                           @Param("category") Category category,
                                           @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.period = :period AND b.active = true")
    List<Budget> findByUserAndPeriodAndActive(@Param("user") User user, 
                                            @Param("period") Budget.BudgetPeriod period);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.endDate < :currentDate AND b.active = true")
    List<Budget> findExpiredBudgets(@Param("user") User user, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT SUM(b.amount) FROM Budget b WHERE b.user = :user AND b.active = true")
    BigDecimal sumActivebudgetAmountByUser(@Param("user") User user);
    
    @Query("SELECT SUM(b.spentAmount) FROM Budget b WHERE b.user = :user AND b.active = true")
    BigDecimal sumSpentAmountByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(b) FROM Budget b WHERE b.user = :user AND b.spentAmount > b.amount")
    long countOverBudgetsByUser(@Param("user") User user);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user ORDER BY b.endDate ASC")
    List<Budget> findByUserOrderByEndDate(@Param("user") User user);
    
    boolean existsByUserAndNameAndActive(User user, String name, boolean active);
}
