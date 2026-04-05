package com.akashzorvyn.zorvynproj.repository;

import com.akashzorvyn.zorvynproj.entity.Transaction;
import com.akashzorvyn.zorvynproj.entity.TransactionType;
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




    @Query("SELECT t FROM Transaction t WHERE t.isDeleted = false " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND ('' = :category OR LOWER(t.category) LIKE LOWER(CONCAT('%', :category, '%'))) " +
           "AND t.date >= :startDate " +
           "AND t.date <= :endDate " +
           "AND (:userId IS NULL OR t.user.id = :userId) " +
           "AND ('' = :keyword OR LOWER(t.category) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(t.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Transaction> findWithFilters(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    // ─── Filtered — USER (own data only, same sentinel convention) ────────────

    @Query("SELECT t FROM Transaction t WHERE t.isDeleted = false " +
           "AND t.user.id = :userId " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND ('' = :category OR LOWER(t.category) LIKE LOWER(CONCAT('%', :category, '%'))) " +
           "AND t.date >= :startDate " +
           "AND t.date <= :endDate " +
           "AND ('' = :keyword OR LOWER(t.category) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(t.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Transaction> findByUserWithFilters(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("keyword") String keyword,
            Pageable pageable);

    // ─── Dashboard summary ─────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.isDeleted = false AND t.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.isDeleted = false AND t.type = :type AND t.user.id = :userId")
    BigDecimal sumByTypeAndUser(@Param("type") TransactionType type, @Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isDeleted = false")
    Long countActive();

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isDeleted = false AND t.user.id = :userId")
    Long countActiveByUser(@Param("userId") Long userId);

    // ─── Dashboard category-wise ───────────────────────────────────────────────

    @Query("SELECT t.category, t.type, SUM(t.amount), COUNT(t) FROM Transaction t " +
           "WHERE t.isDeleted = false GROUP BY t.category, t.type ORDER BY SUM(t.amount) DESC")
    List<Object[]> findCategoryWiseSummary();

    @Query("SELECT t.category, t.type, SUM(t.amount), COUNT(t) FROM Transaction t " +
           "WHERE t.isDeleted = false AND t.user.id = :userId GROUP BY t.category, t.type ORDER BY SUM(t.amount) DESC")
    List<Object[]> findCategoryWiseSummaryByUser(@Param("userId") Long userId);

    // ─── Dashboard monthly trends ──────────────────────────────────────────────

    @Query(value = "SELECT TO_CHAR(t.date, 'YYYY-MM') AS period, t.type, SUM(t.amount) " +
                   "FROM transactions t WHERE t.is_deleted = false " +
                   "GROUP BY TO_CHAR(t.date, 'YYYY-MM'), t.type ORDER BY period DESC",
           nativeQuery = true)
    List<Object[]> findMonthlyTrends();

    @Query(value = "SELECT TO_CHAR(t.date, 'YYYY-MM') AS period, t.type, SUM(t.amount) " +
                   "FROM transactions t WHERE t.is_deleted = false AND t.user_id = :userId " +
                   "GROUP BY TO_CHAR(t.date, 'YYYY-MM'), t.type ORDER BY period DESC",
           nativeQuery = true)
    List<Object[]> findMonthlyTrendsByUser(@Param("userId") Long userId);

    // ─── Dashboard weekly trends ───────────────────────────────────────────────

    @Query(value = "SELECT TO_CHAR(t.date, 'IYYY-IW') AS period, t.type, SUM(t.amount) " +
                   "FROM transactions t WHERE t.is_deleted = false " +
                   "GROUP BY TO_CHAR(t.date, 'IYYY-IW'), t.type ORDER BY period DESC",
           nativeQuery = true)
    List<Object[]> findWeeklyTrends();

    @Query(value = "SELECT TO_CHAR(t.date, 'IYYY-IW') AS period, t.type, SUM(t.amount) " +
                   "FROM transactions t WHERE t.is_deleted = false AND t.user_id = :userId " +
                   "GROUP BY TO_CHAR(t.date, 'IYYY-IW'), t.type ORDER BY period DESC",
           nativeQuery = true)
    List<Object[]> findWeeklyTrendsByUser(@Param("userId") Long userId);

    // ─── Dashboard recent ─────────────────────────────────────────────────────

    List<Transaction> findTop10ByIsDeletedFalseOrderByCreatedAtDesc();

    List<Transaction> findTop10ByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    // ─── Analytics ────────────────────────────────────────────────────────────

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t " +
           "WHERE t.isDeleted = false AND t.type = 'EXPENSE' GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> findExpenseCategoryTotals();

    @Query(value = "SELECT TO_CHAR(t.date, 'YYYY-MM'), SUM(t.amount) " +
                   "FROM transactions t WHERE t.is_deleted = false AND t.type = 'EXPENSE' " +
                   "GROUP BY TO_CHAR(t.date, 'YYYY-MM') ORDER BY TO_CHAR(t.date, 'YYYY-MM') DESC",
           nativeQuery = true)
    List<Object[]> findExpenseMonthlyTotals();
}
