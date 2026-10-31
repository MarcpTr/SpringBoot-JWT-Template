package com.marcptr.jwt.security;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
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
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
   
    private final ObjectMapper mapper;
    private final MessageResolver messageResolver;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(("application/json"));
        ApiResponse<Void> body = ApiResponse.fail(ErrorCode.FORBIDDEN, messageResolver.resolveMessage(ErrorCode.FORBIDDEN), null);
        mapper.writeValue(response.getOutputStream(), body);
    }
}
