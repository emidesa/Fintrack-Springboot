package com.fintrack.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fintrack.api.entity.AuditLog;
import com.fintrack.api.repository.AuditLogRepository;
import com.fintrack.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void log(Long userId, String action, String entityType, Long entityId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        
        if (userId != null) {
            userRepository.findById(userId).ifPresent(auditLog::setUser);
        }
        
        auditLogRepository.save(auditLog);
    }
    
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditsByUser(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
    
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentAudits() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }
}
