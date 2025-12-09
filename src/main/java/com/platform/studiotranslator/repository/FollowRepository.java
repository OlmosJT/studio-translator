package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.entity.FollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<FollowEntity, UUID> {

    // Standard check (active follows only)
    boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    // Standard fetch (active follows only)
    Optional<FollowEntity> findByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    // NATIVE QUERY: Finds the relationship even if it was Soft Deleted.
    // We need this to "resurrect" the link if a user follows, unfollows, then follows again.
    @Query(value = "SELECT * FROM follows WHERE follower_id = :followerId AND followed_id = :followedId",
            nativeQuery = true)
    Optional<FollowEntity> findRawByFollowerAndFollowed(UUID followerId, UUID followedId);

    long countByFollowedId(UUID translatorId);
}
