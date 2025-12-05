package com.platform.studiotranslator.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_review_user_project", columnNames = {"user_id", "project_id"})
        },
        indexes = {
                @Index(name = "idx_review_project", columnList = "project_id"), // Load all reviews for a book
                @Index(name = "idx_review_user", columnList = "user_id")        // Load all reviews by a specific critic
        }
)
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLDelete(sql = "UPDATE reviews SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class ReviewEntity extends AuditableEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    private ProjectEntity project;

    // 1 to 5
    @Column(name = "rating_value", nullable = false)
    private Short ratingValue;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(name = "vote_count")
    private Integer voteCount = 0;
}
