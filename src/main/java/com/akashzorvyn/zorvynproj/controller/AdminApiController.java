package com.akashzorvyn.zorvynproj.controller;

import com.akashzorvyn.zorvynproj.dto.*;
import com.akashzorvyn.zorvynproj.entity.TransactionType;
import com.akashzorvyn.zorvynproj.service.DashboardService;
import com.akashzorvyn.zorvynproj.service.TransactionService;
import jakarta.validation.Valid;
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


@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    private final TransactionService transactionService;
    private final DashboardService dashboardService;



    /* POST /api/admin/transactions — Create transaction for any user */
    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        log.info("ADMIN creating transaction for userId: {}", request.getUserId());
        TransactionDTO dto = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully", dto));
    }

    /** GET /api/admin/transactions — All transactions with full filtering + keyword */
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

    /** GET /api/admin/transactions/{id} — Get single transaction */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Transaction fetched", transactionService.getTransactionById(id)));
    }

    /** PUT /api/admin/transactions/{id} — Update transaction */
    @PutMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request) {
        log.info("ADMIN updating transaction: {}", id);
        TransactionDTO dto = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", dto));
    }

    /** DELETE /api/admin/transactions/{id} — Soft delete */
    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Long id) {
        log.info("ADMIN soft deleting transaction: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully"));
    }


    // DASHBOARD — All Data + Optional userId Filter is down


    /** GET /api/admin/dashboard/summary?userId={id} */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getSummary(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Summary fetched", dashboardService.getSummary(userId)));
    }

    /** GET /api/admin/dashboard/category-wise?userId={id} */
    @GetMapping("/dashboard/category-wise")
    public ResponseEntity<ApiResponse<List<CategorySummaryDTO>>> getCategoryWise(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Category-wise fetched", dashboardService.getCategoryWiseSummary(userId)));
    }

    /** GET /api/admin/dashboard/trends?type=monthly|weekly&userId={id} */
    @GetMapping("/dashboard/trends")
    public ResponseEntity<ApiResponse<List<TrendDTO>>> getTrends(
            @RequestParam(defaultValue = "monthly") String type,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success(type + " trends fetched", dashboardService.getTrends(type, userId)));
    }

    /** GET /api/admin/dashboard/recent?userId={id} */
    @GetMapping("/dashboard/recent")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getRecent(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Recent transactions fetched", dashboardService.getRecentTransactions(userId)));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

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
