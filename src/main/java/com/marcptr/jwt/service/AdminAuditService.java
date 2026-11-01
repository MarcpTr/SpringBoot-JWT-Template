
package com.marcptr.jwt.service;

import com.marcptr.jwt.model.AdminAuditLog;
import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.AdminAuditAction;
import com.marcptr.jwt.repository.AdminAuditRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditRepository adminAuditRepository;

    @Transactional
    public void log(
            AdminAuditAction action,
            UUID targetId,
            String targetUsername) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof User admin)) {
            throw new IllegalStateException(
                    "Unable to identify the administrator performing the action"
            );
        }

        AdminAuditLog auditLog = AdminAuditLog.builder()
                .actorId(admin.getId())
                .actorUsername(admin.getUsername())
                .action(action)
                .targetId(targetId)
                .targetUsername(targetUsername)
                .result("SUCCESS")
                .build();

        adminAuditRepository.save(auditLog);
    }
}
