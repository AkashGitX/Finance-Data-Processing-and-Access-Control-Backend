package com.akashzorvyn.zorvynproj.controller;

import com.akashzorvyn.zorvynproj.dto.*;
import com.akashzorvyn.zorvynproj.entity.TransactionType;
import com.akashzorvyn.zorvynproj.service.TransactionService;
import com.akashzorvyn.zorvynproj.util.SecurityUtils;
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

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final SecurityUtils securityUtils;

    /**
     * POST /api/transactions
     * Create transaction — ADMIN only.
     * Admin can create transactions for any user.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        log.info("Admin creating transaction for userId: {}", request.getUserId());
        TransactionDTO dto = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully", dto));
    }

    /**
     * GET /api/transactions
     * ADMIN/ANALYST → all transactions with filters
     * USER → own transactions only
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'USER')")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        TransactionFilterRequest filter = new TransactionFilterRequest();
        filter.setType(type);
        filter.setCategory(category);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setPage(page);
        filter.setSize(size);

        Page<TransactionDTO> result;

        if (securityUtils.isAdminOrAnalyst()) {
            // ADMIN and ANALYST can see all transactions and filter by any userId
            filter.setUserId(userId);
            result = transactionService.getFilteredTransactions(filter);
        } else {
            // USER can only see their own transactions; userId param is ignored
            Long currentUserId = securityUtils.getCurrentUserId();
            result = transactionService.getUserTransactions(currentUserId, filter);
        }

        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", result));
    }

    /**
     * GET /api/transactions/{id}
     * Get single transaction.
     * ADMIN/ANALYST → any transaction
     * USER → only own transaction
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'USER')")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransaction(@PathVariable Long id) {
        TransactionDTO dto = transactionService.getTransactionById(id);

        // USER: enforce ownership check
        if (!securityUtils.isAdminOrAnalyst()) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!dto.getUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied: You can only view your own transactions"));
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Transaction fetched successfully", dto));
    }

    /**
     * PUT /api/transactions/{id}
     * Update transaction — ADMIN only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request) {
        log.info("Admin updating transaction: {}", id);
        TransactionDTO dto = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", dto));
    }

    /**
     * DELETE /api/transactions/{id}
     * Soft delete transaction — ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Long id) {
        log.info("Admin soft deleting transaction: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully"));
    }
}
