package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.entity.ReviewEntity;
import com.platform.studiotranslator.projection.RatingStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    // Fetch active review for a specific user/book pair
    Optional<ReviewEntity> findByUserIdAndProjectId(UUID userId, UUID projectId);

    // Fetch all reviews for a book (Public Page)
    Page<ReviewEntity> findAllByProjectId(UUID projectId, Pageable pageable);

    // NATIVE: Find even if soft-deleted (to handle re-reviews)
    @Query(value = "SELECT * FROM reviews WHERE user_id = :userId AND project_id = :projectId",
            nativeQuery = true)
    Optional<ReviewEntity> findRawByUserAndProject(UUID userId, UUID projectId);

    // AGGREGATION: Efficiently calculate new stats for the project
    @Query("SELECT COUNT(r) as count, AVG(r.ratingValue) as average FROM ReviewEntity r WHERE r.project.id = :projectId")
    RatingStats findStatsByProjectId(@Param("projectId") UUID projectId);
}
