package com.akashzorvyn.zorvynproj.dto;

import com.akashzorvyn.zorvynproj.entity.TransactionType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionFilterRequest {

    private TransactionType type;
    private String category;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId;
    private String keyword;  // search in category, notes
    private int page = 0;
    private int size = 10;
}
