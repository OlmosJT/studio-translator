package com.platform.studiotranslator.entity;

import com.platform.studiotranslator.constant.AuthProvider;
import com.platform.studiotranslator.constant.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "users")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserEntity extends AuditableEntity implements UserDetails {
    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String displayName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String passwordHash;

    @Column(nullable = true, unique = true)
    private String googleSub;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Column(nullable = true)
    private String avatarUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.READER;

    @ToString.Exclude
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TranslatorEntity translatorProfile;

    @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private Set<FollowEntity> following = new HashSet<>(); // this.user follows the list of translators

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private Set<LibraryEntity> library = new HashSet<>();

    public void setTranslatorProfile(TranslatorEntity translatorProfile) {
        if (translatorProfile == null) {
            if (this.translatorProfile != null) {
                this.translatorProfile.setUser(null);
            }
        } else {
            translatorProfile.setUser(this);
        }
        this.translatorProfile = translatorProfile;
    }

    // --- USER DETAILS ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

}

