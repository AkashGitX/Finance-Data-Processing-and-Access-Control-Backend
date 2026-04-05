package com.akashzorvyn.zorvynproj.dto;

import com.akashzorvyn.zorvynproj.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryDTO {

    private String category;
    private TransactionType type;
    private BigDecimal totalAmount;
    private Long transactionCount;
}
