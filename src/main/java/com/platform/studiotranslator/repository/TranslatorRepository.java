package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.entity.TranslatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TranslatorRepository extends JpaRepository<TranslatorEntity, UUID> {
    // Since Translator ID == User ID, standard findById works.
    // But sometimes you might want to find by the User entity object:
    Optional<TranslatorEntity> findByUserId(UUID userId);
}
