package com.platform.studiotranslator.entity;

import com.platform.studiotranslator.constant.ReadingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "library_entries",
        uniqueConstraints = {
                // A user can only have ONE entry per book.
                // You cannot have "The Moneychangers" as 'READING' and 'DROPPED' at the same time.
                @UniqueConstraint(name = "uk_library_user_project", columnNames = {"user_id", "project_id"})
        },
        indexes = {
                @Index(name = "idx_library_user", columnList = "user_id"),
                @Index(name = "idx_library_status", columnList = "status")
        }
)
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLDelete(sql = "UPDATE library_entries SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class LibraryEntity extends AuditableEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // --- The Owner ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private UserEntity user;

    // --- The Book ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    private ProjectEntity project;

    // --- Progress Tracking ---

    // Pointer to the exact chapter the user last opened.
    // Nullable because a user might add a book to "PLAN_TO_READ" but hasn't started yet.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_chapter_id")
    @ToString.Exclude
    private ChapterEntity lastReadChapter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReadingStatus status = ReadingStatus.PLAN_TO_READ;

    // Optional: Private notes (e.g., "Stopped at the part where the bank crashes")
    @Column(columnDefinition = "TEXT")
    private String userNote;
}
