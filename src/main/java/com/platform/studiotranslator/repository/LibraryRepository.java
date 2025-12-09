package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.constant.ReadingStatus;
import com.platform.studiotranslator.entity.LibraryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LibraryRepository extends JpaRepository<LibraryEntity, UUID> {

    // Standard fetch for active entries
    Optional<LibraryEntity> findByUserIdAndProjectId(UUID userId, UUID projectId);

    // Fetch user's library with optional status filter
    Page<LibraryEntity> findAllByUserIdAndStatus(UUID userId, ReadingStatus status, Pageable pageable);
    Page<LibraryEntity> findAllByUserId(UUID userId, Pageable pageable);

    // NATIVE QUERY: Find entry even if soft-deleted (to handle re-adding)
    @Query(value = "SELECT * FROM library_entries WHERE user_id = :userId AND project_id = :projectId",
            nativeQuery = true)
    Optional<LibraryEntity> findRawByUserAndProject(UUID userId, UUID projectId);
}
