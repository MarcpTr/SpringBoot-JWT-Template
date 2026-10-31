package com.marcptr.jwt.repository;

import com.marcptr.jwt.model.Token;
import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;

public interface TokenRepository extends JpaRepository<Token, UUID> {
        Optional<Token> findByJti(String jti);

        List<Token> findAllByUserAndExpiredFalseAndRevokedFalse(User user);

        List<Token> findAllByUserIdAndTokenTypeAndExpiredFalseAndRevokedFalse(
                        User user,
                        TokenType tokenType);

        Optional<Token> findByToken(String token);

        long countByUser_IdAndTokenTypeAndExpiredFalseAndRevokedFalse(
                        UUID userId,
                        TokenType tokenType);

        List<Token> findAllByUser_IdAndTokenTypeAndExpiredFalseAndRevokedFalse(
                        UUID userId,
                        TokenType tokenType);

        void deleteAllByUser_Id(UUID userId);

        long countByTokenTypeAndExpiredFalseAndRevokedFalse(TokenType tokenType);

        long countByTokenType(TokenType tokenType);
}
