package com.platform.studiotranslator.service;

import com.platform.studiotranslator.constant.ReadingStatus;
import com.platform.studiotranslator.entity.ChapterEntity;
import com.platform.studiotranslator.entity.LibraryEntity;
import com.platform.studiotranslator.entity.ProjectEntity;
import com.platform.studiotranslator.entity.UserEntity;
import com.platform.studiotranslator.repository.ChapterRepository;
import com.platform.studiotranslator.repository.LibraryRepository;
import com.platform.studiotranslator.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final ProjectRepository projectRepository;
    private final ChapterRepository chapterRepository;

    /**
     * Adds a book to library OR updates status if already exists (even if deleted).
     */
    @Transactional
    public void addOrUpdateLibraryEntry(UserEntity user, UUID projectId, ReadingStatus status) {
        // 1. Check if entry exists (Active OR Deleted)
        var existingEntry = libraryRepository.findRawByUserAndProject(user.getId(), projectId);

        if (existingEntry.isPresent()) {
            LibraryEntity entry = existingEntry.get();

            // Restore if deleted
            if (entry.getDeletedAt() != null) {
                entry.setDeletedAt(null);
            }

            // Update status
            entry.setStatus(status);
            libraryRepository.save(entry);
        } else {
            // 2. Create New
            ProjectEntity project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found"));

            LibraryEntity newEntry = LibraryEntity.builder()
                    .user(user)
                    .project(project)
                    .status(status)
                    .build();

            libraryRepository.save(newEntry);
        }
    }

    /**
     * Updates the user's progress pointer (Last Read Chapter).
     * Auto-switches status to READING if currently PLAN_TO_READ.
     */
    @Transactional
    public void updateProgress(UserEntity user, UUID projectId, UUID chapterId) {
        LibraryEntity entry = libraryRepository.findByUserIdAndProjectId(user.getId(), projectId)
                .orElseThrow(() -> new EntityNotFoundException("Book not in library. Add it first."));

        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found"));

        // Validate chapter belongs to project
        if (!chapter.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Chapter does not belong to this project");
        }

        entry.setLastReadChapter(chapter);

        // Quality of Life: Auto-start reading
        if (entry.getStatus() == ReadingStatus.PLAN_TO_READ) {
            entry.setStatus(ReadingStatus.READING);
        }

        libraryRepository.save(entry);
    }

    @Transactional
    public void removeFromLibrary(UserEntity user, UUID projectId) {
        LibraryEntity entry = libraryRepository.findByUserIdAndProjectId(user.getId(), projectId)
                .orElseThrow(() -> new EntityNotFoundException("Entry not found"));

        libraryRepository.delete(entry); // Soft delete
    }

    @Transactional(readOnly = true)
    public Page<LibraryEntity> getUserLibrary(UserEntity user, ReadingStatus status, Pageable pageable) {
        if (status != null) {
            return libraryRepository.findAllByUserIdAndStatus(user.getId(), status, pageable);
        }
        return libraryRepository.findAllByUserId(user.getId(), pageable);
    }
}
