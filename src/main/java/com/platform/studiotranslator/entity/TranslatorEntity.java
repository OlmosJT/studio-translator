package com.platform.studiotranslator.entity;

import com.platform.studiotranslator.constant.TranslatorBadge;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "translators")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLDelete(sql = "UPDATE translators SET deleted_at = NOW(), version = version + 1 WHERE user_id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class TranslatorEntity extends AuditableEntity {
    @Id
    @Column(name = "user_id",  nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private UserEntity user;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String middleName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TranslatorBadge badge = TranslatorBadge.HOBBYIST;

    @OneToMany(mappedBy = "followed", fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private Set<FollowEntity> followers = new HashSet<>(); // translator has list of users following him

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "translator_languages",
            joinColumns = @JoinColumn(name = "translator_id")
    )
    @Builder.Default
    private Set<LanguageSkill> languages = new HashSet<>();

    @Builder.Default
    private Integer totalProjects = 0;

    @Builder.Default
    private Integer totalFollowers = 0;
}
