package com.akashzorvyn.zorvynproj.controller;

import com.akashzorvyn.zorvynproj.dto.*;
import com.akashzorvyn.zorvynproj.entity.TransactionType;
import com.akashzorvyn.zorvynproj.service.DashboardService;
import com.akashzorvyn.zorvynproj.service.TransactionService;
import com.akashzorvyn.zorvynproj.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * USER (VIEWER) prefixed API endpoints — Own data only.
 * All routes: /api/user/**
 * All methods require ROLE_USER.
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserApiController {

    private final TransactionService transactionService;
    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    // ════════════════════════════════════════════════════════════════════════
    // TRANSACTIONS — Own Only
    // ════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/user/transactions
     * Own transactions with limited filtering: type, category, startDate, endDate
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getMyTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = securityUtils.getCurrentUserId();
        TransactionFilterRequest filter = buildFilter(type, category, startDate, endDate, page, size);
        Page<TransactionDTO> result = transactionService.getUserTransactions(userId, filter);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", result));
    }

    /**
     * GET /api/user/transactions/{id}
     * Get own transaction by ID (403 if not owner)
     */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getMyTransaction(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        TransactionDTO dto = transactionService.getTransactionById(id);
        if (!dto.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: This transaction does not belong to you"));
        }
        return ResponseEntity.ok(ApiResponse.success("Transaction fetched", dto));
    }

    // ════════════════════════════════════════════════════════════════════════
    // DASHBOARD — Own Data Only
    // ════════════════════════════════════════════════════════════════════════

    /** GET /api/user/dashboard/summary — Own income/expense/net balance */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getMySummary() {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Summary fetched", dashboardService.getSummary(userId)));
    }

    /** GET /api/user/dashboard/category-wise — Own category breakdown */
    @GetMapping("/dashboard/category-wise")
    public ResponseEntity<ApiResponse<List<CategorySummaryDTO>>> getMyCategoryWise() {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Category-wise fetched", dashboardService.getCategoryWiseSummary(userId)));
    }

    /** GET /api/user/dashboard/trends?type=monthly|weekly — Own trends */
    @GetMapping("/dashboard/trends")
    public ResponseEntity<ApiResponse<List<TrendDTO>>> getMyTrends(
            @RequestParam(defaultValue = "monthly") String type) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(type + " trends fetched", dashboardService.getTrends(type, userId)));
    }

    /** GET /api/user/dashboard/recent — Own 10 most recent transactions */
    @GetMapping("/dashboard/recent")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getMyRecent() {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Recent transactions fetched", dashboardService.getRecentTransactions(userId)));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private TransactionFilterRequest buildFilter(TransactionType type, String category,
                                                  LocalDate startDate, LocalDate endDate,
                                                  int page, int size) {
        TransactionFilterRequest f = new TransactionFilterRequest();
        f.setType(type);
        f.setCategory(category);
        f.setStartDate(startDate);
        f.setEndDate(endDate);
        f.setPage(page);
        f.setSize(size);
        return f;
    }
}
