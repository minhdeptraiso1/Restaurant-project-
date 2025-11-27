package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.CreateUserReq;
import com.hoabanrestaurant.backend.dto.request.UpdateInforUserReq;
import com.hoabanrestaurant.backend.dto.response.UserDto;
import com.hoabanrestaurant.backend.entity.User;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.UserRole;
import com.hoabanrestaurant.backend.enums.UserStatus;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.mapper.UserMapper;
import com.hoabanrestaurant.backend.repository.UserRepository;
import com.hoabanrestaurant.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repo;

    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Page<UserDto> findAllUsers(Pageable pageable, String search, String role, String status) {
        Page<User> page = (search != null && !search.isEmpty())
                ? repo.findByFullNameContainingIgnoreCase(search, pageable)
                : repo.findAll(pageable);

        return page.map(mapper::toDto);
    }

    @Override
    public UserDto findById(UUID id) {
        User user = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return mapper.toDto(user);
    }

    @Override
    public UserDto create(CreateUserReq req) { //create coustomer user
        if (repo.findByEmail(req.email()).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }
        User u = new User();
        u.setFullName(req.fullName());
        u.setEmail(req.email());
        u.setPhone(req.phone());
        u.setRole(UserRole.CUSTOMER);
        u.setStatus(UserStatus.ACTIVE);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        User saved = repo.save(u);
        return mapper.toDto(saved);
    }

    @Override
    public UserDto update(UUID id, UpdateInforUserReq req) {
        User user = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (req.fullName() != null && !req.fullName().isBlank()) user.setFullName(req.fullName());
        if (req.phone() != null && !req.phone().isBlank()) user.setPhone(req.phone());
        if (req.address() != null) user.setAddress(req.address());
        if (req.avatarUrl() != null) user.setAvatarUrl(req.avatarUrl());

        User updated = repo.save(user);
        return mapper.toDto(updated);
    }

    @Override
    public void delete(UUID id) {
        User user = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        repo.delete(user);
    }

    @Override
    public UserDto updateRole(UUID id, String role) {
        User user = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setRole(UserRole.valueOf(role));
        User updated = repo.save(user);
        return mapper.toDto(updated);
    }

}
