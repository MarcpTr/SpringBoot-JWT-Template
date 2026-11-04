
package com.marcptr.jwt.dto.response;

import com.marcptr.jwt.model.AdminAuditLog;
import com.marcptr.jwt.model.enums.AdminAuditAction;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminAuditResponse(
        UUID id,
        UUID actorId,
        String actorUsername,
        AdminAuditAction action,
        UUID targetId,
        String targetUsername,
        String result,
        LocalDateTime createdAt
) {
    public static AdminAuditResponse fromEntity(AdminAuditLog auditLog) {
        return new AdminAuditResponse(
                auditLog.getId(),
                auditLog.getActorId(),
                auditLog.getActorUsername(),
                auditLog.getAction(),
                auditLog.getTargetId(),
                auditLog.getTargetUsername(),
                auditLog.getResult(),
                auditLog.getCreatedAt()
        );
    }
}
