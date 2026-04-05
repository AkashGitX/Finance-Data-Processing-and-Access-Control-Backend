package com.akashzorvyn.zorvynproj.controller;

import com.akashzorvyn.zorvynproj.dto.*;
import com.akashzorvyn.zorvynproj.entity.TransactionType;
import com.akashzorvyn.zorvynproj.service.AnalyticsService;
import com.akashzorvyn.zorvynproj.service.DashboardService;
import com.akashzorvyn.zorvynproj.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ANALYST-prefixed API endpoints — Read-only + AI Analytics.
 * All routes: /api/analyst/**
 * All methods require ROLE_ANALYST.
 */
@Slf4j
@RestController
@RequestMapping("/api/analyst")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ANALYST')")
public class AnalystApiController {

    private final TransactionService transactionService;
    private final DashboardService dashboardService;
    private final AnalyticsService analyticsService;

    // ════════════════════════════════════════════════════════════════════════
    // TRANSACTIONS — All Data, Read Only
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/analyst/transactions
     * Supports full filtering: type, category, startDate, endDate, userId, keyword
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        TransactionFilterRequest filter = buildFilter(type, category, startDate, endDate, userId, keyword, page, size);
        Page<TransactionDTO> result = transactionService.getFilteredTransactions(filter);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", result));
    }

    /** GET /api/analyst/transactions/{id} */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Transaction fetched", transactionService.getTransactionById(id)));
    }

    // ════════════════════════════════════════════════════════════════════════
    // DASHBOARD — All Users Data (optional userId filter)
    // ════════════════════════════════════════════════════════════════════════

    /** GET /api/analyst/dashboard/summary?userId={id} */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getSummary(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Summary fetched", dashboardService.getSummary(userId)));
    }

    /** GET /api/analyst/dashboard/category-wise */
    @GetMapping("/dashboard/category-wise")
    public ResponseEntity<ApiResponse<List<CategorySummaryDTO>>> getCategoryWise() {
        return ResponseEntity.ok(ApiResponse.success("Category-wise fetched", dashboardService.getCategoryWiseSummary(null)));
    }

    /** GET /api/analyst/dashboard/trends?type=monthly|weekly */
    @GetMapping("/dashboard/trends")
    public ResponseEntity<ApiResponse<List<TrendDTO>>> getTrends(
            @RequestParam(defaultValue = "monthly") String type) {
        return ResponseEntity.ok(ApiResponse.success(type + " trends fetched", dashboardService.getTrends(type, null)));
    }

    /** GET /api/analyst/dashboard/recent */
    @GetMapping("/dashboard/recent")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getRecent() {
        return ResponseEntity.ok(ApiResponse.success("Recent transactions fetched", dashboardService.getRecentTransactions(null)));
    }

    // ════════════════════════════════════════════════════════════════════════
    // AI ANALYTICS — ANALYST ONLY
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/analyst/analytics/expense-pattern
     * Aggregates all expenses → Spring AI analysis.
     * Prompt: "Analyze the expense data and identify patterns, trends, anomalies,
     *          high spending categories, and monthly growth."
     */
    @GetMapping("/analytics/expense-pattern")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeExpensePattern() {
        log.info("ANALYST requesting expense pattern analysis");
        return ResponseEntity.ok(ApiResponse.success("Expense pattern analysis complete",
                analyticsService.analyzeExpensePattern()));
    }

    /**
     * GET /api/analyst/analytics/income-vs-expense
     * Fetches income/expense totals → Spring AI insights.
     * Prompt: "Compare income and expenses and provide insights on financial health,
     *          savings trends, and potential risks."
     */
    @GetMapping("/analytics/income-vs-expense")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeIncomeVsExpense() {
        log.info("ANALYST requesting income vs expense analysis");
        return ResponseEntity.ok(ApiResponse.success("Income vs expense analysis complete",
                analyticsService.analyzeIncomeVsExpense()));
    }



    private TransactionFilterRequest buildFilter(TransactionType type, String category,
                                                  LocalDate startDate, LocalDate endDate,
                                                  Long userId, String keyword, int page, int size) {
        TransactionFilterRequest f = new TransactionFilterRequest();
        f.setType(type);
        f.setCategory(category);
        f.setStartDate(startDate);
        f.setEndDate(endDate);
        f.setUserId(userId);
        f.setKeyword(keyword);
        f.setPage(page);
        f.setSize(size);
        return f;
    }
}
