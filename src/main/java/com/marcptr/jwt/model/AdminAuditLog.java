
package com.marcptr.jwt.model;

import com.marcptr.jwt.model.enums.AdminAuditAction;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID actorId;

    @Column(nullable = false)
    private String actorUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminAuditAction action;

    @Column(nullable = false)
    private UUID targetId;

    private String targetUsername;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String result = "SUCCESS";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
