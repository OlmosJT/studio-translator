package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.entity.FollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<FollowEntity, UUID> {

    // "Is User A following Translator B?"
    boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    // Fetch the follow relationship (e.g., to delete it)
    Optional<FollowEntity> findByFollowerIdAndFollowedId(UUID followerId, UUID followedId);
}
