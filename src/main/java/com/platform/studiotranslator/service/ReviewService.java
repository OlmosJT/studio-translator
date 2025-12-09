package com.platform.studiotranslator.service;

import com.platform.studiotranslator.entity.ProjectEntity;
import com.platform.studiotranslator.entity.ReviewEntity;
import com.platform.studiotranslator.entity.UserEntity;
import com.platform.studiotranslator.projection.RatingStats;
import com.platform.studiotranslator.repository.ProjectRepository;
import com.platform.studiotranslator.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public void addOrUpdateReview(UserEntity user, UUID projectId, Short rating, String content) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // 1. Check for existing (Active OR Deleted)
        var existingReview = reviewRepository.findRawByUserAndProject(user.getId(), projectId);

        if (existingReview.isPresent()) {
            ReviewEntity review = existingReview.get();

            // Restore if deleted (Re-reviewing)
            if (review.getDeletedAt() != null) {
                review.setDeletedAt(null);
            }

            // Update content
            review.setRatingValue(rating);
            review.setContent(content);
            reviewRepository.save(review);
        } else {
            // 2. Create New
            ProjectEntity project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found"));

            ReviewEntity newReview = ReviewEntity.builder()
                    .user(user)
                    .project(project)
                    .ratingValue(rating)
                    .content(content)
                    .voteCount(0)
                    .build();

            reviewRepository.save(newReview);
        }

        // 3. Update Project Stats (Average Rating)
        updateProjectStats(projectId);
    }

    @Transactional
    public void deleteReview(UserEntity user, UUID projectId) {
        ReviewEntity review = reviewRepository.findByUserIdAndProjectId(user.getId(), projectId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        reviewRepository.delete(review); // Soft Delete

        // Recalculate stats (removing this rating from average)
        updateProjectStats(projectId);
    }

    /**
     * Helper: Recalculates Average Rating and Total Review Count
     * and saves it to the ProjectEntity.
     */
    private void updateProjectStats(UUID projectId) {
        RatingStats stats = reviewRepository.findStatsByProjectId(projectId);

        projectRepository.findById(projectId).ifPresent(project -> {
            project.setTotalReviews(stats.getCount() != null ? stats.getCount().intValue() : 0);
            project.setAverageRating(stats.getAverage() != null ? stats.getAverage() : 0.0);
            projectRepository.save(project);
        });
    }

    @Transactional(readOnly = true)
    public Page<ReviewEntity> getProjectReviews(UUID projectId, Pageable pageable) {
        return reviewRepository.findAllByProjectId(projectId, pageable);
    }
}
