package com.fintrack.api.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fintrack.api.entity.Transaction;
import com.fintrack.api.enums.Category;
import com.fintrack.api.enums.TransactionStatus;
import com.fintrack.api.enums.TransactionType;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    List<Transaction> findByTransactionType(TransactionType type);
    
    List<Transaction> findByCategory(Category category);
    
    List<Transaction> findByCreatedById(Long userId);
    
    List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.status IN :statuses")
    List<Transaction> findByDateRangeAndStatuses(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("statuses") List<TransactionStatus> statuses
    );
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.transactionType = :type " +
           "AND t.status IN ('VALIDEE', 'FINALISEE') " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByTypeAndDateRange(
        @Param("type") TransactionType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(
        @Param("status") TransactionStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT t FROM Transaction t WHERE t.amount > :threshold " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findSuspiciousTransactions(
        @Param("threshold") BigDecimal threshold,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
