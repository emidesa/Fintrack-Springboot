package com.fintrack.api.controller;

import com.fintrack.api.dto.TransactionCreateRequest;
import com.fintrack.api.dto.TransactionUpdateRequest;
import com.fintrack.api.dto.ApiResponse;
import com.fintrack.api.dto.TransactionResponse;
import com.fintrack.api.enums.Category;
import com.fintrack.api.enums.TransactionStatus;
import com.fintrack.api.enums.TransactionType;
import com.fintrack.api.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('COMPTABLE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionCreateRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        TransactionResponse response = transactionService.createTransaction(request, userEmail);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction créée avec succès", response));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(@PathVariable Long id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
    
    @GetMapping("/my-transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getMyTransactions(
            Authentication authentication) {
        String userEmail = authentication.getName();
        List<TransactionResponse> transactions = transactionService.getMyTransactions(userEmail);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByStatus(
            @PathVariable TransactionStatus status) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
    
    @GetMapping("/type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByType(
            @PathVariable TransactionType type) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByType(type);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
    
    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByCategory(
            @PathVariable Category category) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        TransactionResponse response = transactionService.updateTransaction(id, request, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Transaction modifiée avec succès", response));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        transactionService.deleteTransaction(id, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Transaction supprimée avec succès", null));
    }
    
    @PatchMapping("/{id}/validate")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponse>> validateTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        TransactionResponse response = transactionService.validateTransaction(id, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Transaction validée avec succès", response));
    }
    
    @PatchMapping("/{id}/finalize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponse>> finalizeTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        TransactionResponse response = transactionService.finalizeTransaction(id, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Transaction finalisée avec succès", response));
    }
    
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponse>> rejectTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        TransactionResponse response = transactionService.rejectTransaction(id, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Transaction rejetée avec succès", response));
    }
}
