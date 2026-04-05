package com.akashzorvyn.zorvynproj.controller;

import com.akashzorvyn.zorvynproj.dto.*;
import com.akashzorvyn.zorvynproj.service.DashboardService;
import com.akashzorvyn.zorvynproj.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    /**
     * GET /api/dashboard/summary
     * USER     → own summary only
     * ANALYST  → all users (no userId filter override)
     * ADMIN    → all users + optional ?userId=X filter
     *
     * All values calculated dynamically — never stored.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getSummary(
            @RequestParam(required = false) Long userId) {

        Long resolvedUserId = resolveUserId(userId);
        DashboardSummaryDTO summary = dashboardService.getSummary(resolvedUserId);
        return ResponseEntity.ok(ApiResponse.success("Summary fetched successfully", summary));
    }

    /**
     * GET /api/dashboard/category-wise
     * USER     → own categories
     * ANALYST  → all categories
     * ADMIN    → all categories + optional ?userId=X filter
     */
    @GetMapping("/category-wise")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<List<CategorySummaryDTO>>> getCategoryWise(
            @RequestParam(required = false) Long userId) {

        Long resolvedUserId = securityUtils.isAdmin() ? userId : null;
        List<CategorySummaryDTO> result = dashboardService.getCategoryWiseSummary(resolvedUserId);
        return ResponseEntity.ok(ApiResponse.success("Category-wise summary fetched", result));
    }

    /**
     * GET /api/dashboard/trends?type=monthly|weekly
     * USER     → own trends
     * ANALYST  → all trends
     * ADMIN    → all trends + optional ?userId=X filter
     */
    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<List<TrendDTO>>> getTrends(
            @RequestParam(defaultValue = "monthly") String type,
            @RequestParam(required = false) Long userId) {

        Long resolvedUserId = securityUtils.isAdmin() ? userId : null;
        List<TrendDTO> trends = dashboardService.getTrends(type, resolvedUserId);
        return ResponseEntity.ok(ApiResponse.success(type + " trends fetched successfully", trends));
    }

    /**
     * GET /api/dashboard/recent
     * USER     → own recent transactions
     * ANALYST  → all recent
     * ADMIN    → all recent + optional ?userId=X filter
     */
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getRecent(
            @RequestParam(required = false) Long userId) {

        Long resolvedUserId = securityUtils.isAdmin() ? userId : null;
        List<TransactionDTO> recent = dashboardService.getRecentTransactions(resolvedUserId);
        return ResponseEntity.ok(ApiResponse.success("Recent transactions fetched", recent));
    }

    /**
     * Resolves userId based on role:
     * USER     → always their own ID (override any passed userId)
     * ANALYST  → no filter (null = all)
     * ADMIN    → uses ?userId param if provided, else all
     */
    private Long resolveUserId(Long requestedUserId) {
        if (securityUtils.isAdmin()) {
            return requestedUserId; // may be null → all users
        } else if (securityUtils.isAnalyst()) {
            return null; // analyst always sees all
        } else {
            return securityUtils.getCurrentUserId(); // user sees only own
        }
    }
}
