
package com.marcptr.jwt.repository;

import com.marcptr.jwt.model.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminAuditRepository extends JpaRepository<AdminAuditLog, UUID> {
}
