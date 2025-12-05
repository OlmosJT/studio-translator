package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    // Ensure a user hasn't already reviewed this book
    boolean existsByUserIdAndProjectId(UUID userId, UUID projectId);
}
