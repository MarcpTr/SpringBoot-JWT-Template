package com.marcptr.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.marcptr.jwt.dto.request.RegisterRequest;
import com.marcptr.jwt.exception.ResourceAlreadyExistsException;
import com.marcptr.jwt.dto.request.LoginRequest;
import com.marcptr.jwt.dto.request.RefreshRequest;
import com.marcptr.jwt.dto.response.AuthResponse;
import com.marcptr.jwt.dto.response.RefreshResponse;
import com.marcptr.jwt.exception.InvalidCredentialsException;
import com.marcptr.jwt.exception.JwtAuthenticationException;
import com.marcptr.jwt.model.Token;
import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.ErrorCode;
import com.marcptr.jwt.model.enums.TokenType;
import com.marcptr.jwt.repository.TokenRepository;
import com.marcptr.jwt.repository.UserRepository;
import com.marcptr.jwt.utils.MessageResolver;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;
    private final SessionPolicyService sessionPolicyService;
    private final MessageResolver messageResolver;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

   @Transactional
public AuthResponse login(LoginRequest request) {

    UsernamePasswordAuthenticationToken authRequest =
            new UsernamePasswordAuthenticationToken(
                    request.usernameOrEmail(),
                    request.password());

    Authentication auth;

    try {
        auth = authenticationManager.authenticate(authRequest);
    } catch (AuthenticationException e) {
        throw new InvalidCredentialsException(ErrorCode.INVALID_CREDENTIALS);
    }

    User authenticatedUser = (User) auth.getPrincipal();

    User user = userRepository.findByIdForUpdate(authenticatedUser.getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));

    int maxSessions = sessionPolicyService.getMaxSessions();

    tokenService.enforceSessionLimit(user, maxSessions);

    String refreshToken = jwtService.generateRefreshToken(user);
    String accessToken = jwtService.generateAccessToken(user, refreshToken);

    tokenService.saveUserToken(user, refreshToken, TokenType.REFRESH);

    return new AuthResponse(
            new AuthResponse.User(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername()),
            accessToken,
            refreshToken);
}
    public RefreshResponse refresh(RefreshRequest request) {
        String refreshToken = request.refreshToken();

        Claims claims;
        try {
            claims = jwtService.extractClaim(refreshToken, c -> c);
        } catch (JwtException e) {
            throw new JwtAuthenticationException(ErrorCode.INVALID_TOKEN,
                    messageResolver.resolveMessage(ErrorCode.INVALID_TOKEN));
        }

        String username = claims.getSubject();
        String jti = claims.getId();

        User user = (User) userService.loadUserByUsername(username);

        Token storedToken = tokenRepository.findByJti(jti)
                .orElseThrow(() -> new JwtAuthenticationException(ErrorCode.INVALID_TOKEN,
                        messageResolver.resolveMessage(ErrorCode.TOKEN_NOT_FOUND)));

        if (storedToken.isExpired() || storedToken.isRevoked()) {
            throw new JwtAuthenticationException(ErrorCode.INVALID_TOKEN,
                    messageResolver.resolveMessage(ErrorCode.INVALID_TOKEN));
        }

        if (storedToken.getTokenType() != TokenType.REFRESH) {
            throw new JwtAuthenticationException(ErrorCode.INVALID_TOKEN,
                    messageResolver.resolveMessage(ErrorCode.INVALID_TOKEN_TYPE));
        }

        if (!jwtService.isTokenValid(refreshToken, user, TokenType.REFRESH)) {
            throw new JwtAuthenticationException(ErrorCode.INVALID_TOKEN,
                    messageResolver.resolveMessage(ErrorCode.INVALID_TOKEN));
        }

        tokenService.revokeToken(refreshToken);

        String newRefreshToken = jwtService.generateRefreshToken(user);
        String newAccessToken = jwtService.generateAccessToken(user, newRefreshToken);

        tokenService.saveUserToken(user, newRefreshToken, TokenType.REFRESH);

        return new RefreshResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public AuthResponse.User register(RegisterRequest request) {

        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        if (userService.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (userService.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(com.marcptr.jwt.model.enums.Role.ROLE_USER)
                .build();

        User savedUser = userService.save(user);

        return new AuthResponse.User(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername());
    }
}
