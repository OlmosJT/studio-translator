package com.platform.studiotranslator.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comments",
        indexes = {
                @Index(name = "idx_comment_chapter", columnList = "chapter_id"),
                @Index(name = "idx_comment_parent", columnList = "parent_id")
        }
)
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLDelete(sql = "UPDATE comments SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class CommentEntity extends AuditableEntity {

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
    @JoinColumn(name = "chapter_id", nullable = false)
    @ToString.Exclude
    private ChapterEntity chapter;

    // --- Threading (Replies) ---

    // If null, this is a top-level comment. If set, it's a reply.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @ToString.Exclude
    private CommentEntity parent;

    // Helper to fetch replies easily (One-to-Many self-reference)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<CommentEntity> replies = new ArrayList<>();

    // --- Content & Flags ---

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(name = "is_spoiler", nullable = false)
    private boolean isSpoiler = false;

    @Builder.Default
    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;
}
