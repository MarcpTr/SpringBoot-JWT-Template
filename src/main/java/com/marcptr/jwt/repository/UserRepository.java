
package com.marcptr.jwt.repository;

import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.Role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

        Optional<User> findByUsername(String username);

        Optional<User> findByEmail(String email);

        boolean existsByRole(Role roleAdmin);

        boolean existsByUsernameAndIdNot(String username, UUID id);

        boolean existsByEmailAndIdNot(String email, UUID id);

        @Query("""
                        SELECT u FROM User u
                        WHERE (:username IS NULL OR
                               LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
                          AND (:email IS NULL OR
                               LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
                          AND (:role IS NULL OR u.role = :role)
                          AND (:enabled IS NULL OR u.enabled = :enabled)
                        """)
        Page<User> findAllWithFilters(
                        @Param("username") String username,
                        @Param("email") String email,
                        @Param("role") Role role,
                        @Param("enabled") Boolean enabled,
                        Pageable pageable);

        long countByEnabledTrue();

        long countByEnabledFalse();

        long countByRole(Role role);

        long countByRoleAndEnabledTrue(Role role);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT u FROM User u WHERE u.id = :id")
        Optional<User> findByIdForUpdate(@Param("id") UUID id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT u FROM User u
                        WHERE u.role = :role
                        ORDER BY u.id
                        """)
        List<User> findAllByRoleForUpdate(@Param("role") Role role);
}
