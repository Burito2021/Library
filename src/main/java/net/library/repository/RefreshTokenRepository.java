// src/main/java/net/library/repository/RefreshTokenRepository.java
package net.library.repository;

import net.library.model.entity.RefreshToken;
import net.library.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(User user);

//    @Modifying
//    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
//    void deleteExpiredTokens(LocalDateTime now);
}
