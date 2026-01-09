package com.fintrack.api.service;

import com.fintrack.api.dto.LoginRequest;
import com.fintrack.api.dto.UserCreateRequest;
import com.fintrack.api.dto.UserUpdateRequest;
import com.fintrack.api.dto.LoginResponse;
import com.fintrack.api.dto.UserResponse;
import com.fintrack.api.entity.User;
import com.fintrack.api.enums.Role;
import com.fintrack.api.exception.ConflictException;
import com.fintrack.api.exception.ResourceNotFoundException;
import com.fintrack.api.mapper.UserMapper;
import com.fintrack.api.repository.UserRepository;
import com.fintrack.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder; 
    private final JwtUtil jwtUtil; 
    
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Un utilisateur avec cet email existe déjà");
        }
        
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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
    
    // Méthode de login
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));
        
        if (!user.getIsActive()) {
            throw new BadCredentialsException("Ce compte est désactivé");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }
        
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }
    
    //Pour récupérer un User par email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email: " + email));
    }
}