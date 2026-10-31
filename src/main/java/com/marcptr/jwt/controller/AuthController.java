package com.marcptr.jwt.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.marcptr.jwt.dto.common.ApiResponse;
import com.marcptr.jwt.dto.request.LoginRequest;
import com.marcptr.jwt.dto.request.RefreshRequest;
import com.marcptr.jwt.dto.response.AuthResponse;
import com.marcptr.jwt.dto.response.RefreshResponse;
import com.marcptr.jwt.service.AuthService;
import com.marcptr.jwt.dto.request.RegisterRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        System.out.println(request.toString());
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse.User>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse.User user = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(user));
    }
}
