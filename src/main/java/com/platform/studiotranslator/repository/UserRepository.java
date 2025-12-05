package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    // Used for "Forgot Password" or Registration checks
    Optional<UserEntity> findByDisplayName(String displayName);

    // Used during Registration validation
    boolean existsByEmail(String email);
    boolean existsByDisplayName(String displayName);

    // Used for OAuth (Google) login
    Optional<UserEntity> findByGoogleSub(String googleSub);
}
