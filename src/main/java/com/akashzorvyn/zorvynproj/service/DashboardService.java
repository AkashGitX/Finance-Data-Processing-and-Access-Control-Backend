package com.akashzorvyn.zorvynproj.service;

import com.akashzorvyn.zorvynproj.dto.CategorySummaryDTO;
import com.akashzorvyn.zorvynproj.dto.DashboardSummaryDTO;
import com.akashzorvyn.zorvynproj.dto.TransactionDTO;
import com.akashzorvyn.zorvynproj.dto.TrendDTO;
import com.akashzorvyn.zorvynproj.entity.TransactionType;
import com.akashzorvyn.zorvynproj.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    // ---------- SUMMARY ----------

    public DashboardSummaryDTO getSummary(Long userId) {
        log.info("Fetching dashboard summary for userId: {}", userId != null ? userId : "ALL");
        BigDecimal totalIncome;
        BigDecimal totalExpense;
        Long totalTransactions;

        if (userId != null) {
            totalIncome = transactionRepository.sumByTypeAndUser(TransactionType.INCOME, userId);
            totalExpense = transactionRepository.sumByTypeAndUser(TransactionType.EXPENSE, userId);
            totalTransactions = transactionRepository.countActiveByUser(userId);
        } else {
            totalIncome = transactionRepository.sumByType(TransactionType.INCOME);
            totalExpense = transactionRepository.sumByType(TransactionType.EXPENSE);
            totalTransactions = transactionRepository.countActive();
        }

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        return DashboardSummaryDTO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .totalTransactions(totalTransactions)
                .userId(userId)
                .build();
    }

    // ---------- CATEGORY-WISE ----------

    public List<CategorySummaryDTO> getCategoryWiseSummary(Long userId) {
        log.info("Fetching category-wise summary for userId: {}", userId != null ? userId : "ALL");
        List<Object[]> rawData = (userId != null)
                ? transactionRepository.findCategoryWiseSummaryByUser(userId)
                : transactionRepository.findCategoryWiseSummary();

        List<CategorySummaryDTO> result = new ArrayList<>();
        for (Object[] row : rawData) {
            result.add(CategorySummaryDTO.builder()
                    .category((String) row[0])
                    .type(TransactionType.valueOf(row[1].toString()))
                    .totalAmount((BigDecimal) row[2])
                    .transactionCount((Long) row[3])
                    .build());
        }
        return result;
    }

    // ---------- TRENDS ----------

    public List<TrendDTO> getTrends(String type, Long userId) {
        log.info("Fetching {} trends for userId: {}", type, userId != null ? userId : "ALL");
        List<Object[]> rawData;

        if ("weekly".equalsIgnoreCase(type)) {
            rawData = (userId != null)
                    ? transactionRepository.findWeeklyTrendsByUser(userId)
                    : transactionRepository.findWeeklyTrends();
        } else {
            rawData = (userId != null)
                    ? transactionRepository.findMonthlyTrendsByUser(userId)
                    : transactionRepository.findMonthlyTrends();
        }

        Map<String, TrendDTO> trendMap = new LinkedHashMap<>();

        for (Object[] row : rawData) {
            String period = (String) row[0];
            TransactionType txType = TransactionType.valueOf(row[1].toString());
            BigDecimal amount = (BigDecimal) row[2];

            trendMap.computeIfAbsent(period, p -> TrendDTO.builder()
                    .period(p)
                    .totalIncome(BigDecimal.ZERO)
                    .totalExpense(BigDecimal.ZERO)
                    .netBalance(BigDecimal.ZERO)
                    .build());

            TrendDTO trend = trendMap.get(period);
            if (txType == TransactionType.INCOME) {
                trend.setTotalIncome(amount);
            } else {
                trend.setTotalExpense(amount);
            }
            trend.setNetBalance(trend.getTotalIncome().subtract(trend.getTotalExpense()));
        }

        return new ArrayList<>(trendMap.values());
    }

    // ---------- RECENT ----------

    public List<TransactionDTO> getRecentTransactions(Long userId) {
        log.info("Fetching recent transactions for userId: {}", userId != null ? userId : "ALL");
        if (userId != null) {
            return transactionRepository
                    .findTop10ByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(TransactionDTO::fromEntity)
                    .toList();
        }
        return transactionRepository
                .findTop10ByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(TransactionDTO::fromEntity)
                .toList();
    }
}
