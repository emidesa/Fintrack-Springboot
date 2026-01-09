package com.fintrack.api.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.fintrack.api.dto.TransactionCreateRequest;
import com.fintrack.api.dto.TransactionResponse;
import com.fintrack.api.dto.TransactionUpdateRequest;
import com.fintrack.api.entity.Transaction;
import com.fintrack.api.entity.User;
import com.fintrack.api.enums.TransactionStatus;

@Component
public class TransactionMapper {
    
    private final ModelMapper modelMapper;
    private final UserMapper userMapper;
    
    public TransactionMapper(ModelMapper modelMapper, UserMapper userMapper) {
        this.modelMapper = modelMapper;
        this.userMapper = userMapper;
    }
    
    public Transaction toEntity(TransactionCreateRequest request, User createdBy) {
        Transaction transaction = modelMapper.map(request, Transaction.class);
        transaction.setCreatedBy(createdBy);
        transaction.setStatus(TransactionStatus.EN_ATTENTE);
        return transaction;
    }
    
    public TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = modelMapper.map(transaction, TransactionResponse.class);
        
        // Mapper les relations utilisateur
        if (transaction.getCreatedBy() != null) {
            response.setCreatedBy(userMapper.toResponse(transaction.getCreatedBy()));
        }
        if (transaction.getValidatedBy() != null) {
            response.setValidatedBy(userMapper.toResponse(transaction.getValidatedBy()));
        }
        if (transaction.getFinalizedBy() != null) {
            response.setFinalizedBy(userMapper.toResponse(transaction.getFinalizedBy()));
        }
        
        return response;
    }
    
    public void updateEntityFromRequest(TransactionUpdateRequest request, Transaction transaction) {
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getTransactionType() != null) {
            transaction.setTransactionType(request.getTransactionType());
        }
        if (request.getCategory() != null) {
            transaction.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }
    }
}
