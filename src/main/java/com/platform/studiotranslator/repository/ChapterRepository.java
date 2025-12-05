package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.constant.ChapterStatus;
import com.platform.studiotranslator.entity.ChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChapterRepository extends JpaRepository<ChapterEntity, UUID> {

    // Find a specific chapter in a book
    Optional<ChapterEntity> findByProjectIdAndChapterNumber(UUID projectId, Double chapterNumber);

    // Used during Sync to find the chapter associated with a Doc ID
    Optional<ChapterEntity> findByGoogleDocId(String googleDocId);

    List<ChapterEntity> findAllByProjectIdOrderByChapterNumberAsc(UUID projectId);
    List<ChapterEntity> findAllByProjectIdAndStatusOrderByChapterNumberAsc(UUID projectId, ChapterStatus status);
}
