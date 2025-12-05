package com.platform.studiotranslator.entity;

import com.platform.studiotranslator.constant.Language;
import com.platform.studiotranslator.constant.ProjectGenre;
import com.platform.studiotranslator.constant.ProjectStatus;
import com.platform.studiotranslator.constant.ProjectType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "projects", indexes = {
        @Index(name = "idx_project_slug", columnList = "slug", unique = true),
        @Index(name = "idx_project_status", columnList = "status"),
        @Index(name = "idx_project_langs", columnList = "target_language, original_language") // Optimization for filtering
})
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLDelete(sql = "UPDATE projects SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class ProjectEntity extends AuditableEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "translator_id", nullable = false)
    @ToString.Exclude
    private TranslatorEntity translator;

    // --- Identification ---
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    // --- Source Material Info ---
    @Column(name = "original_author", nullable = false)
    private String originalAuthor;

    @Column(name = "source_link")
    private String sourceLink;

    // --- Language Info (STRICT ENUMS) ---

    @Enumerated(EnumType.STRING)
    @Column(name = "original_language", nullable = false, length = 5)
    private Language originalLanguage; // e.g., EN

    @Enumerated(EnumType.STRING)
    @Column(name = "target_language", nullable = false, length = 5)
    private Language targetLanguage;   // e.g., UZ

    // --- Categorization ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type; // FICTION, NON_FICTION, TECHNICAL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ONGOING;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "project_genres",
            joinColumns = @JoinColumn(name = "project_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "genre")
    @Builder.Default
    private Set<ProjectGenre> genres = new HashSet<>();

    // --- Stats ---
    @Builder.Default
    @Column(name = "chapter_count")
    private Integer chapterCount = 0;

    @Builder.Default
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Builder.Default
    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Builder.Default
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;
}
