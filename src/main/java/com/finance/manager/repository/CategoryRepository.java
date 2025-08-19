package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByUser(User user);
    
    List<Category> findByUserAndType(User user, Category.CategoryType type);
    
    Optional<Category> findByUserAndName(User user, String name);
    
    @Query("SELECT c FROM Category c WHERE c.user = :user AND c.type = :type ORDER BY c.name ASC")
    List<Category> findByUserAndTypeOrderByName(@Param("user") User user, 
                                               @Param("type") Category.CategoryType type);
    
    @Query("SELECT c FROM Category c WHERE c.user = :user ORDER BY c.type ASC, c.name ASC")
    List<Category> findByUserOrderByTypeAndName(@Param("user") User user);
    
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.user = :user AND c.name = :name AND c.id != :excludeId")
    boolean existsByUserAndNameExcludingId(@Param("user") User user, 
                                          @Param("name") String name, 
                                          @Param("excludeId") Long excludeId);
    
    boolean existsByUserAndName(User user, String name);
    
    @Query("SELECT COUNT(c) FROM Category c WHERE c.user = :user AND c.type = :type")
    long countByUserAndType(@Param("user") User user, @Param("type") Category.CategoryType type);
}
