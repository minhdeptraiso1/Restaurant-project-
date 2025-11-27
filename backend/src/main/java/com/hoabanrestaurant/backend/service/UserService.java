package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.CreateUserReq;
import com.hoabanrestaurant.backend.dto.request.UpdateInforUserReq;
import com.hoabanrestaurant.backend.dto.response.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {


    Page<UserDto> findAllUsers(Pageable pageable, String search, String role, String status);

    UserDto findById(UUID id);

    UserDto update(UUID id, UpdateInforUserReq req);

    UserDto create(CreateUserReq req);

    void delete(UUID id);

    UserDto updateRole(UUID id, String role);


}
