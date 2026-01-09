package com.fintrack.api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fintrack.api.dto.TransactionCreateRequest;
import com.fintrack.api.dto.TransactionResponse;
import com.fintrack.api.dto.TransactionUpdateRequest;
import com.fintrack.api.entity.Transaction;
import com.fintrack.api.entity.User;
import com.fintrack.api.enums.Category;
import com.fintrack.api.enums.Role;
import com.fintrack.api.enums.TransactionStatus;
import com.fintrack.api.enums.TransactionType;
import com.fintrack.api.exception.BadRequestException;
import com.fintrack.api.exception.ResourceNotFoundException;
import com.fintrack.api.exception.UnauthorizedException;
import com.fintrack.api.mapper.TransactionMapper;
import com.fintrack.api.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserService userService;
    private final AuditService auditService;
    
    @Transactional
    public TransactionResponse createTransaction(TransactionCreateRequest request, String userEmail) {
        User creator = userService.findByEmail(userEmail);
        
        // Validation : montant positif
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant doit être positif");
        }
        
        Transaction transaction = transactionMapper.toEntity(request, creator);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Log d'audit
        auditService.log(creator.getId(), "CREATE_TRANSACTION", "Transaction", 
                        savedTransaction.getId(), 
                        "Création transaction " + request.getTransactionType() + " de " + request.getAmount());
        
        return transactionMapper.toResponse(savedTransaction);
    }
    
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = findTransactionById(id);
        return transactionMapper.toResponse(transaction);
    }
    
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status)
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByType(TransactionType type) {
        return transactionRepository.findByTransactionType(type)
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByCategory(Category category) {
        return transactionRepository.findByCategory(category)
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate)
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions(String userEmail) {
        User user = userService.findByEmail(userEmail);
        return transactionRepository.findByCreatedById(user.getId())
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionUpdateRequest request, String userEmail) {
        Transaction transaction = findTransactionById(id);
        User user = userService.findByEmail(userEmail);
        
        // Vérifier les permissions : seul le créateur ou un ADMIN peut modifier
        if (!transaction.getCreatedBy().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier cette transaction");
        }
        
        // On ne peut pas modifier une transaction finalisée
        if (transaction.getStatus() == TransactionStatus.FINALISEE) {
            throw new BadRequestException("Impossible de modifier une transaction finalisée");
        }
        
        // Validation du montant si présent
        if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant doit être positif");
        }
        
        transactionMapper.updateEntityFromRequest(request, transaction);
        Transaction updatedTransaction = transactionRepository.save(transaction);
        
        // Log d'audit
        auditService.log(user.getId(), "UPDATE_TRANSACTION", "Transaction", 
                        updatedTransaction.getId(), 
                        "Modification de la transaction #" + id);
        
        return transactionMapper.toResponse(updatedTransaction);
    }
    
    @Transactional
    public void deleteTransaction(Long id, String userEmail) {
        Transaction transaction = findTransactionById(id);
        User user = userService.findByEmail(userEmail);
        
        // Seul un ADMIN peut supprimer une transaction
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Seul un ADMIN peut supprimer une transaction");
        }
        
        // Log d'audit avant suppression
        auditService.log(user.getId(), "DELETE_TRANSACTION", "Transaction", 
                        transaction.getId(), 
                        "Suppression de la transaction #" + id);
        
        transactionRepository.delete(transaction);
    }
    
    @Transactional
    public TransactionResponse validateTransaction(Long id, String userEmail) {
        Transaction transaction = findTransactionById(id);
        User validator = userService.findByEmail(userEmail);
        
        // Seul un MANAGER peut valider
        if (validator.getRole() != Role.MANAGER && validator.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Seul un MANAGER ou ADMIN peut valider une transaction");
        }
        
        // La transaction doit être EN_ATTENTE
        if (transaction.getStatus() != TransactionStatus.EN_ATTENTE) {
            throw new BadRequestException("Seule une transaction EN_ATTENTE peut être validée");
        }
        
        transaction.setStatus(TransactionStatus.VALIDEE);
        transaction.setValidatedBy(validator);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Log d'audit
        auditService.log(validator.getId(), "VALIDATE_TRANSACTION", "Transaction", 
                        savedTransaction.getId(), 
                        "Validation de la transaction #" + id);
        
        return transactionMapper.toResponse(savedTransaction);
    }
    
    @Transactional
    public TransactionResponse finalizeTransaction(Long id, String userEmail) {
        Transaction transaction = findTransactionById(id);
        User finalizer = userService.findByEmail(userEmail);
        
        // Seul un ADMIN peut finaliser
        if (finalizer.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Seul un ADMIN peut finaliser une transaction");
        }
        
        // La transaction doit être VALIDEE
        if (transaction.getStatus() != TransactionStatus.VALIDEE) {
            throw new BadRequestException("Seule une transaction VALIDEE peut être finalisée");
        }
        
        transaction.setStatus(TransactionStatus.FINALISEE);
        transaction.setFinalizedBy(finalizer);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Log d'audit
        auditService.log(finalizer.getId(), "FINALIZE_TRANSACTION", "Transaction", 
                        savedTransaction.getId(), 
                        "Finalisation de la transaction #" + id);
        
        return transactionMapper.toResponse(savedTransaction);
    }
    
    @Transactional
    public TransactionResponse rejectTransaction(Long id, String userEmail) {
        Transaction transaction = findTransactionById(id);
        User rejector = userService.findByEmail(userEmail);
        
        // MANAGER ou ADMIN peuvent rejeter
        if (rejector.getRole() != Role.MANAGER && rejector.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Seul un MANAGER ou ADMIN peut rejeter une transaction");
        }
        
        // On ne peut pas rejeter une transaction finalisée
        if (transaction.getStatus() == TransactionStatus.FINALISEE) {
            throw new BadRequestException("Impossible de rejeter une transaction finalisée");
        }
        
        transaction.setStatus(TransactionStatus.REJETEE);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Log d'audit
        auditService.log(rejector.getId(), "REJECT_TRANSACTION", "Transaction", 
                        savedTransaction.getId(), 
                        "Rejet de la transaction #" + id);
        
        return transactionMapper.toResponse(savedTransaction);
    }
    
    private Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction non trouvée avec l'ID: " + id));
    }
}