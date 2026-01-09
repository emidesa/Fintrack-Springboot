package com.fintrack.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fintrack.api.enums.Category;
import com.fintrack.api.enums.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionUpdateRequest {
    
    @Positive(message = "Le montant doit être positif")
    @DecimalMin(value = "0.01", message = "Le montant minimum est 0.01")
    private BigDecimal amount;
    
    private TransactionType transactionType;
    
    private Category category;
    
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;
    
    @PastOrPresent(message = "La date ne peut pas être dans le futur")
    private LocalDate transactionDate;
}

