package com.fintrack.api.mapper;

import com.fintrack.api.dto.UserCreateRequest;
import com.fintrack.api.dto.UserUpdateRequest;
import com.fintrack.api.dto.UserResponse;
import com.fintrack.api.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    private final ModelMapper modelMapper;
    
    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }
    
    public User toEntity(UserCreateRequest request) {
        return modelMapper.map(request, User.class);
    }
    
    public UserResponse toResponse(User user) {
        return modelMapper.map(user, UserResponse.class);
    }
    
    public void updateEntityFromRequest(UserUpdateRequest request, User user) {
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
    }
}

