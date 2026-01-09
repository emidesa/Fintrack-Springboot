package com.fintrack.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fintrack.api.enums.Category;
import com.fintrack.api.enums.TransactionStatus;
import com.fintrack.api.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType transactionType;
    private Category category;
    private TransactionStatus status;
    private String description;
    private LocalDate transactionDate;
    private UserResponse createdBy;
    private UserResponse validatedBy;
    private UserResponse finalizedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

