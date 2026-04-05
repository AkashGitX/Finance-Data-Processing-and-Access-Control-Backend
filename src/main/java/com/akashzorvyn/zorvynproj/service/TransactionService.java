package com.akashzorvyn.zorvynproj.service;

import com.akashzorvyn.zorvynproj.dto.TransactionDTO;
import com.akashzorvyn.zorvynproj.dto.TransactionFilterRequest;
import com.akashzorvyn.zorvynproj.dto.TransactionRequest;
import com.akashzorvyn.zorvynproj.dto.TransactionUpdateRequest;
import com.akashzorvyn.zorvynproj.entity.Transaction;
import com.akashzorvyn.zorvynproj.entity.User;
import com.akashzorvyn.zorvynproj.exception.ResourceNotFoundException;
import com.akashzorvyn.zorvynproj.repository.TransactionRepository;
import com.akashzorvyn.zorvynproj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // ─── ADMIN: Create ────────────────────────────────────────────────────────

    @Transactional
    public TransactionDTO createTransaction(TransactionRequest request) {
        log.info("Creating transaction for userId: {}", request.getUserId());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .notes(request.getNotes())
                .user(user)
                .isDeleted(false)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created — id: {}", saved.getId());
        return TransactionDTO.fromEntity(saved);
    }

    // ─── ADMIN: Update ────────────────────────────────────────────────────────

    @Transactional
    public TransactionDTO updateTransaction(Long id, TransactionUpdateRequest request) {
        log.info("Updating transaction: {}", id);
        Transaction t = getActiveTransaction(id);
        if (request.getAmount() != null) t.setAmount(request.getAmount());
        if (request.getType() != null)   t.setType(request.getType());
        if (request.getCategory() != null) t.setCategory(request.getCategory());
        if (request.getDate() != null)   t.setDate(request.getDate());
        if (request.getNotes() != null)  t.setNotes(request.getNotes());
        return TransactionDTO.fromEntity(transactionRepository.save(t));
    }

    // ─── ADMIN: Soft Delete ───────────────────────────────────────────────────

    @Transactional
    public void deleteTransaction(Long id) {
        log.info("Soft deleting transaction: {}", id);
        Transaction t = getActiveTransaction(id);
        t.setDeleted(true);
        transactionRepository.save(t);
    }

    // ─── ADMIN / ANALYST: Get all with full filters ───────────────────────────

    public Page<TransactionDTO> getFilteredTransactions(TransactionFilterRequest filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return transactionRepository.findWithFilters(
                filter.getType(),
                blankIfNull(filter.getCategory()),
                sentinelStart(filter.getStartDate()),
                sentinelEnd(filter.getEndDate()),
                filter.getUserId(),
                blankIfNull(filter.getKeyword()),
                pageable
        ).map(TransactionDTO::fromEntity);
    }

    // ─── USER: Own transactions with limited filters ──────────────────────────

    public Page<TransactionDTO> getUserTransactions(Long userId, TransactionFilterRequest filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return transactionRepository.findByUserWithFilters(
                userId,
                filter.getType(),
                blankIfNull(filter.getCategory()),
                sentinelStart(filter.getStartDate()),
                sentinelEnd(filter.getEndDate()),
                blankIfNull(filter.getKeyword()),
                pageable
        ).map(TransactionDTO::fromEntity);
    }

    // ─── Single lookup ────────────────────────────────────────────────────────

    public TransactionDTO getTransactionById(Long id) {
        return TransactionDTO.fromEntity(getActiveTransaction(id));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Transaction getActiveTransaction(Long id) {
        return transactionRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }

    /** Convert null strings to "" so JPQL can infer PostgreSQL type via '' = :param sentinel. */
    private String blankIfNull(String s) { return s != null ? s : ""; }

    /** No lower-date filter → very old sentinel date. */
    private LocalDate sentinelStart(LocalDate d) { return d != null ? d : LocalDate.of(1000, 1, 1); }

    /** No upper-date filter → far-future sentinel date. */
    private LocalDate sentinelEnd(LocalDate d) { return d != null ? d : LocalDate.of(9999, 12, 31); }
}
