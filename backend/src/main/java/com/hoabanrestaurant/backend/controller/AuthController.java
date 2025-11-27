package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.ChangePasswordRequest;
import com.hoabanrestaurant.backend.dto.request.LoginRequest;
import com.hoabanrestaurant.backend.dto.request.RefreshRequest;
import com.hoabanrestaurant.backend.dto.request.RegisterRequest;
import com.hoabanrestaurant.backend.dto.request.UpdateInforUserReq;
import com.hoabanrestaurant.backend.dto.response.AuthResponse;
import com.hoabanrestaurant.backend.dto.response.UserDto;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.AuthService;
import com.hoabanrestaurant.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                           HttpServletRequest http) {
        return ApiResponse.ok(authService.login(req, http));
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@AuthenticationPrincipal Jwt jwt) {
        authService.logout(jwt.getId(), jwt.getExpiresAt());
        return ApiResponse.ok("Logged out");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserDto> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/change-password")
    public ApiResponse<?> changePassword(@AuthenticationPrincipal Jwt jwt,
                                         @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(jwt.getSubject(), req);
        return ApiResponse.ok("Đổi mật khẩu thành công");
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ApiResponse.ok(authService.refresh(req.refreshToken()));
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        return ApiResponse.ok(authService.me(email));
    }

    @PutMapping("/profile")
    public ApiResponse<UserDto> updateInfor(@AuthenticationPrincipal Jwt jwt,
                                            @Valid @RequestBody UpdateInforUserReq req) {
        UUID userId = UUID.fromString(jwt.getClaim("uid"));
        return ApiResponse.ok(userService.update(userId, req));
    }

}
