package com.platform.studiotranslator.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "follows",
        uniqueConstraints = {
                // Note: With Soft Delete, we must handle "Re-following" by un-deleting the row in your Service.
                @UniqueConstraint(name = "uk_follower_followed", columnNames = {"follower_id", "followed_id"})
        },
        indexes = {
                @Index(name = "idx_follow_follower", columnList = "follower_id"), // "Who am I following?"
                @Index(name = "idx_follow_followed", columnList = "followed_id")  // "Who follows this translator?"
        }
)
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLDelete(sql = "UPDATE follows SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class FollowEntity extends AuditableEntity {
    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    @ToString.Exclude
    private UserEntity follower;

    // "Readers cannot follow readers" because Readers don't exist in this table.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false)
    @ToString.Exclude
    private TranslatorEntity followed;

    @Builder.Default
    @Column(name = "receive_notifications", nullable = false)
    private boolean receiveNotifications = true;

}
