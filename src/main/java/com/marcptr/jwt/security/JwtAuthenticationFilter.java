package com.marcptr.jwt.security;

import java.io.IOException;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.marcptr.jwt.model.Token;
import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.TokenType;
import com.marcptr.jwt.service.JwtService;
import com.marcptr.jwt.service.TokenService;
import com.marcptr.jwt.service.UserService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        if (jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                String username = jwtService.extractUsername(jwt);

                if (username != null) {

                    UserDetails userDetails =
                            userService.loadUserByUsername(username);

                    boolean validAccessToken = jwtService.isTokenValid(
                            jwt,
                            userDetails,
                            TokenType.ACCESS
                    );

                    if (validAccessToken) {

                        String refreshJti = jwtService.extractRefreshJti(jwt);

                        if (refreshJti != null) {

                            Token refreshToken =
                                    tokenService.findByJti(refreshJti);

                            User tokenUser = refreshToken.getUser();
                            User authenticatedUser = (User) userDetails;

                            boolean validSession =
                                    !refreshToken.isExpired()
                                    && !refreshToken.isRevoked()
                                    && refreshToken.getTokenType() == TokenType.REFRESH
                                    && tokenUser.getId().equals(authenticatedUser.getId());

                            if (validSession) {

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities()
                                        );

                                authentication.setDetails(
                                        new WebAuthenticationDetailsSource()
                                                .buildDetails(request)
                                );

                                SecurityContextHolder.getContext()
                                        .setAuthentication(authentication);
                            }
                        }
                    }
                }
            }

        } catch (JwtException
                | IllegalArgumentException
                | AuthenticationException e) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}