package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.entity.LibraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LibraryRepository extends JpaRepository<LibraryEntity, UUID> {

    // Get the library entry for a specific user and book
    Optional<LibraryEntity> findByUserIdAndProjectId(UUID userId, UUID projectId);
}
