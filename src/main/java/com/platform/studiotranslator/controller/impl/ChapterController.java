package com.platform.studiotranslator.controller.impl;

import com.platform.studiotranslator.constant.Role;
import com.platform.studiotranslator.controller.ChapterPublicApi;
import com.platform.studiotranslator.controller.ChapterTranslatorApi;
import com.platform.studiotranslator.dto.chapter.ChapterRequest;
import com.platform.studiotranslator.dto.chapter.ChapterResponse;
import com.platform.studiotranslator.entity.UserEntity;
import com.platform.studiotranslator.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChapterController implements ChapterPublicApi, ChapterTranslatorApi {

    private final ChapterService chapterService;

    // --- TRANSLATOR API IMPLEMENTATION ---

    @Override
    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    public ResponseEntity<ChapterResponse> create(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody @Valid ChapterRequest request
    ) {
        return ResponseEntity.ok(chapterService.createChapter(user, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    public ResponseEntity<ChapterResponse> syncAndPublish(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserEntity user
    ) {
        // 1. Fetch content from Google Doc API
        // 2. Convert to HTML/Text
        // 3. Save to DB and mark as PUBLISHED
        return ResponseEntity.ok(chapterService.syncAndPublish(id, user));
    }

    // --- PUBLIC API IMPLEMENTATION ---

    @Override
    public ResponseEntity<ChapterResponse> read(@PathVariable UUID id) {
        return ResponseEntity.ok(chapterService.readChapter(id));
    }

    @Override
    public ResponseEntity<List<ChapterResponse>> getTableOfContents(
            @PathVariable String slug,
            @AuthenticationPrincipal UserEntity user
    ) {
        // Default: Guest mode
        boolean canSeeDrafts = false;

        // Check if user is logged in
        if (user != null) {
            // OPTIONAL: strict check (only show drafts if they own THIS specific project)
            // For now: Admins/Translators can see drafts
            if (user.getRole() == Role.TRANSLATOR || user.getRole() == Role.ADMIN) {
                canSeeDrafts = true;
            }
        }

        return ResponseEntity.ok(chapterService.getChaptersByProjectSlug(slug, canSeeDrafts));
    }
}
