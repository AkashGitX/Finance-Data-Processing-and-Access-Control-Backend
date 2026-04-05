package com.akashzorvyn.zorvynproj.controller;

import com.akashzorvyn.zorvynproj.dto.ApiResponse;
import com.akashzorvyn.zorvynproj.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics/expense-pattern
     * ANALYST ONLY — fetches all expenses, aggregates category-wise totals
     * and monthly trends, sends structured data to Spring AI for pattern analysis.
     *
     * Prompt: "Analyze the expense data and identify patterns, trends,
     *          anomalies, high spending categories, and monthly growth."
     */
    @GetMapping("/expense-pattern")
    @PreAuthorize("hasRole('ANALYST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeExpensePattern() {
        log.info("Analyst requesting expense pattern analysis");
        Map<String, Object> result = analyticsService.analyzeExpensePattern();
        return ResponseEntity.ok(ApiResponse.success("Expense pattern analysis complete", result));
    }

    /**
     * GET /api/analytics/income-vs-expense
     * ANALYST ONLY — fetches total income and expenses, sends structured
     * data to Spring AI for financial health insights.
     *
     * Prompt: "Compare income and expenses and provide insights on
     *          financial health, savings trends, and potential risks."
     */
    @GetMapping("/income-vs-expense")
    @PreAuthorize("hasRole('ANALYST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeIncomeVsExpense() {
        log.info("Analyst requesting income vs expense analysis");
        Map<String, Object> result = analyticsService.analyzeIncomeVsExpense();
        return ResponseEntity.ok(ApiResponse.success("Income vs expense analysis complete", result));
    }
}
