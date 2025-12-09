package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.entity.TranslatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TranslatorRepository extends JpaRepository<TranslatorEntity, UUID> {
    // Since Translator ID == User ID, standard findById works.
    // But sometimes you might want to find by the User entity object:
    Optional<TranslatorEntity> findByUserId(UUID userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE TranslatorEntity t SET t.totalFollowers = t.totalFollowers + 1 WHERE t.id = :id")
    void incrementFollowerCount(@Param("id") UUID id);

    // Added 'AND t.totalFollowers > 0' check to prevent negative numbers
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE TranslatorEntity t SET t.totalFollowers = t.totalFollowers - 1 WHERE t.id = :id AND t.totalFollowers > 0")
    void decrementFollowerCount(@Param("id") UUID id);
}
