package com.akashzorvyn.zorvynproj.dto;

import com.akashzorvyn.zorvynproj.entity.Transaction;
import com.akashzorvyn.zorvynproj.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private String notes;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;

    public static TransactionDTO fromEntity(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .date(transaction.getDate())
                .notes(transaction.getNotes())
                .userId(transaction.getUser().getId())
                .userName(transaction.getUser().getName())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
