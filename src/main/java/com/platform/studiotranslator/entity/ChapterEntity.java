package com.platform.studiotranslator.entity;

import com.platform.studiotranslator.constant.ChapterStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chapters",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_project_chapter_num", columnNames = {"project_id", "chapter_number"})
        },
        indexes = {
                @Index(name = "idx_chapter_project", columnList = "project_id"),
                @Index(name = "idx_chapter_status", columnList = "status")
        }
)
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLDelete(sql = "UPDATE chapters SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class ChapterEntity extends AuditableEntity {
    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    private ProjectEntity project;

    // --- Ordering & Metadata ---

    @Column(name = "chapter_number", nullable = false)
    private Double chapterNumber;

    @Column(nullable = false)
    private String title;

    // --- Google Docs Integration ---

    @Column(name = "google_doc_id", nullable = false, unique = true)
    private String googleDocId;

    // Convenient link for the Translator to click "Edit Chapter" in your UI
    @Column(name = "google_doc_url", nullable = false)
    private String googleDocUrl;

    // --- Content Storage ---

    // The actual text fetched from Google Docs API.
    // LAZY fetch: We don't want to load the whole book text when just showing the chapter list.
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    // --- Status & Stats ---

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChapterStatus status = ChapterStatus.DRAFT;

    @Builder.Default
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Builder.Default
    @Column(name = "word_count")
    private Integer wordCount = 0;
}
