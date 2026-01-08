package com.fintrack.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fintrack.api.dto.UserCreateRequest;
import com.fintrack.api.dto.UserResponse;
import com.fintrack.api.dto.UserUpdateRequest;
import com.fintrack.api.entity.User;
import com.fintrack.api.enums.Role;
import com.fintrack.api.exception.ConflictException;
import com.fintrack.api.exception.ResourceNotFoundException;
import com.fintrack.api.mapper.UserMapper;
import com.fintrack.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Un utilisateur avec cet email existe déjà");
        }
        
        User user = userMapper.toEntity(request);
        user.setIsActive(true);
        User savedUser = userRepository.save(user);
        
        return userMapper.toResponse(savedUser);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        return userMapper.toResponse(user);
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByStatus(Boolean isActive) {
        return userRepository.findByIsActive(isActive)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Cet email est déjà utilisé");
            }
        }
        
        userMapper.updateEntityFromRequest(request, user);
        User updatedUser = userRepository.save(user);
        
        return userMapper.toResponse(updatedUser);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        userRepository.delete(user);
    }
    
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        user.setIsActive(false);
        userRepository.save(user);
    }
}