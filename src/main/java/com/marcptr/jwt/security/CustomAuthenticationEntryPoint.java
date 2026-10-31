package com.marcptr.jwt.security;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcptr.jwt.dto.common.ApiResponse;
import com.marcptr.jwt.model.enums.ErrorCode;
import com.marcptr.jwt.utils.MessageResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;
    private final MessageResolver messageResolver;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        ApiResponse<Void> body = ApiResponse.fail(ErrorCode.UNAUTHORIZED,
                messageResolver.resolveMessage(ErrorCode.UNAUTHORIZED), null);
        mapper.writeValue(response.getOutputStream(), body);
    }

}
