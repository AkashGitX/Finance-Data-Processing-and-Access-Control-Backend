package com.akashzorvyn.zorvynproj.dto;

import com.akashzorvyn.zorvynproj.entity.TransactionType;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionUpdateRequest {

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private TransactionType type;
    private String category;
    private LocalDate date;
    private String notes;
}
