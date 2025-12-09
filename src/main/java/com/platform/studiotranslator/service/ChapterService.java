package com.platform.studiotranslator.service;

import com.platform.studiotranslator.constant.ChapterStatus;
import com.platform.studiotranslator.constant.Role;
import com.platform.studiotranslator.dto.chapter.ChapterRequest;
import com.platform.studiotranslator.dto.chapter.ChapterResponse;
import com.platform.studiotranslator.entity.ChapterEntity;
import com.platform.studiotranslator.entity.ProjectEntity;
import com.platform.studiotranslator.entity.UserEntity;
import com.platform.studiotranslator.mapper.ChapterMapper;
import com.platform.studiotranslator.repository.ChapterRepository;
import com.platform.studiotranslator.repository.ProjectRepository;
import com.platform.studiotranslator.service.googledoc.GoogleWorkspaceService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterRepository chapterRepository;
    private final ProjectRepository projectRepository;
    private final GoogleWorkspaceService  googleService;

    private final ChapterMapper chapterMapper;

    @Transactional
    public ChapterResponse createChapter(UserEntity user, ChapterRequest request) {

        ProjectEntity project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        validateOwnership(project, user);

        if (chapterRepository.findByProjectIdAndChapterNumber(project.getId(), request.chapterNumber()).isPresent()) {
            throw new IllegalArgumentException("Chapter number " + request.chapterNumber() + " already exists.");
        }

        String docTitle = String.format("[%s] Ch %.1f - %s", project.getTitle(), request.chapterNumber(), request.title());

        String docId = googleService.createDocument(docTitle);

        // 2. Share with Translator (so they can actually edit it!)
        // The Service Account owns the file, we give the Translator 'Writer' permission.
        googleService.shareDocument(docId, user.getEmail());

        String googleDocUrl = "https://docs.google.com/document/d/" + docId + "/edit";

        // D. Save Entity
        ChapterEntity chapter = ChapterEntity.builder()
                .project(project)
                .chapterNumber(request.chapterNumber())
                .title(request.title())
                .googleDocId(docId)
                .googleDocUrl(googleDocUrl)
                .status(ChapterStatus.DRAFT)
                .wordCount(0)
                .viewCount(0L)
                .build();

        // Update Project Stats (Optional: Update total chapters)
        project.setChapterCount(project.getChapterCount() + 1);
        projectRepository.save(project);

        return mapToResponse(chapterRepository.save(chapter), true);
    }

    // --- 2. SYNC & PUBLISH ---
    @Transactional
    public ChapterResponse syncAndPublish(UUID chapterId, UserEntity user) {
        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));

        validateOwnership(chapter.getProject(), user);

        // A. Fetch Content from Google
        String htmlContent = googleService.getDocumentContent(chapter.getGoogleDocId());

        // B. Calculate Stats
        int wordCount = countWords(htmlContent); // Helper method

        // C. Update Entity
        chapter.setContent(htmlContent);
        chapter.setWordCount(wordCount);
        chapter.setLastSyncedAt(Instant.now());
        chapter.setStatus(ChapterStatus.PUBLISHED);

        return mapToResponse(chapterRepository.save(chapter), true);
    }


    @Transactional
    public ChapterResponse readChapter(UUID chapterId) {
        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));

        // Rule: Public can only read PUBLISHED chapters
        if (chapter.getStatus() != ChapterStatus.PUBLISHED) {
            throw new SecurityException("This chapter is not published yet.");
        }

        // Increment View Count
        chapter.setViewCount(chapter.getViewCount() + 1);
        chapterRepository.save(chapter);

        return mapToResponse(chapter, false); // False = hide google doc link
    }

    @Transactional(readOnly = true)
    public List<ChapterResponse> getChaptersByProject(UUID projectId, boolean isTranslator) {
        // If Translator: Show ALL (Drafts + Published) so they can see their work
        // If Reader: Show ONLY Published
        List<ChapterEntity> chapters;

        if (isTranslator) {
            chapters = chapterRepository.findAllByProjectIdOrderByChapterNumberAsc(projectId);
        } else {
            chapters = chapterRepository.findAllByProjectIdAndStatusOrderByChapterNumberAsc(
                    projectId, ChapterStatus.PUBLISHED
            );
        }

        // Map list
        return chapters.stream()
                .map(c -> mapToResponse(c, isTranslator)) // isTranslator=true reveals Google Doc Links
                .toList();
    }


    // --- HELPERS ---

    private void validateOwnership(ProjectEntity project, UserEntity user) {
        boolean isOwner = project.getTranslator().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new SecurityException("You do not own this project");
        }
    }

    private int countWords(String html) {
        if (html == null || html.isEmpty()) return 0;
        // Strip HTML tags and count words
        String text = html.replaceAll("<[^>]*>", " ");
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    private ChapterResponse mapToResponse(ChapterEntity c, boolean includeSensitive) {
        return new ChapterResponse(
                c.getId(),
                c.getChapterNumber(),
                c.getTitle(),
                c.getContent(),
                c.getWordCount(),
                c.getViewCount(),
                c.getUpdatedAt(),
                c.getStatus(),
                includeSensitive ? c.getGoogleDocUrl() : null, // Hide URL from readers
                includeSensitive ? c.getLastSyncedAt() : null
        );
    }

    @Transactional(readOnly = true)
    public List<ChapterResponse> getChaptersByProjectSlug(String slug, boolean canSeeDrafts) {
        // 1. Resolve Slug -> Project ID
        // We need to find the project first to ensure it exists and get its UUID
        ProjectEntity project = projectRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with slug: " + slug));

        List<ChapterEntity> chapters;

        // 2. Fetch Chapters using the Project ID
        if (canSeeDrafts) {
            // Translator View: Show everything (Drafts + Published)
            chapters = chapterRepository.findAllByProjectIdOrderByChapterNumberAsc(project.getId());
        } else {
            // Reader View: Show ONLY Published
            chapters = chapterRepository.findAllByProjectIdAndStatusOrderByChapterNumberAsc(
                    project.getId(),
                    ChapterStatus.PUBLISHED
            );
        }

        // 3. Map to DTO
        return chapters.stream()
                .map(c -> mapToResponse(c, canSeeDrafts))
                .toList();
    }

    public ChapterResponse updateChapterStatus(UUID chapterId, @NotNull(message = "Status is required") ChapterStatus newStatus, UserEntity user) {
        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found"));

        if(!chapter.getProject().getTranslator().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own this chapter");
        }

        if (newStatus == ChapterStatus.PUBLISHED) {
            throw new IllegalArgumentException("Use the 'Sync & Publish' endpoint to publish chapters.");
        }

        chapter.setStatus(newStatus);

        ChapterEntity savedChapter = chapterRepository.save(chapter);

        return chapterMapper.toResponse(savedChapter);
    }
}
