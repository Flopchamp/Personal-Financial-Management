package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUser(User user);
    
    Page<Transaction> findByUser(User user, Pageable pageable);
    
    List<Transaction> findByUserAndCategory(User user, Category category);
    
    List<Transaction> findByUserAndType(User user, Transaction.TransactionType type);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date BETWEEN :startDate AND :endDate ORDER BY t.date DESC")
    List<Transaction> findByUserAndDateBetween(@Param("user") User user, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date BETWEEN :startDate AND :endDate ORDER BY t.date DESC")
    Page<Transaction> findByUserAndDateBetween(@Param("user") User user, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate,
                                              Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.category = :category AND t.date BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserAndCategoryAndDateBetween(@Param("user") User user,
                                                         @Param("category") Category category,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = :type")
    BigDecimal sumAmountByUserAndType(@Param("user") User user, 
                                     @Param("type") Transaction.TransactionType type);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndTypeAndDateBetween(@Param("user") User user,
                                                   @Param("type") Transaction.TransactionType type,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.category = :category AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndCategoryAndDateBetween(@Param("user") User user,
                                                       @Param("category") Category category,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user AND t.date BETWEEN :startDate AND :endDate")
    long countByUserAndDateBetween(@Param("user") User user,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user ORDER BY t.date DESC")
    Page<Transaction> findByUserOrderByDateDesc(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND (LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Transaction> findByUserAndDescriptionOrNotesContaining(@Param("user") User user, 
                                                               @Param("keyword") String keyword);
}
