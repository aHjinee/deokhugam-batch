package com.sbproject.deokhugam.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sbproject.deokhugam.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query(value = "DELETE FROM users WHERE id = :userId", nativeQuery = true)
    void hardDeleteById(@Param("userId") UUID userId);

    @Modifying
    @Query(value = "DELETE FROM users WHERE deleted_at IS NOT NULL AND deleted_at < :threshold", nativeQuery = true)
    int deleteExpiredUsers(@Param("threshold") java.time.Instant threshold);
}