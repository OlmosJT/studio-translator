package com.platform.studiotranslator.controller;

import com.platform.studiotranslator.dto.chapter.ChapterRequest;
import com.platform.studiotranslator.dto.chapter.ChapterResponse;
import com.platform.studiotranslator.dto.chapter.UpdateChapterStatusRequest;
import com.platform.studiotranslator.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Chapter Translator API",
        description = "Management endpoints for Translators (Google Docs Sync)"
)
@RequestMapping("/api/chapters")
@SecurityRequirement(name = "bearerAuth")
public interface ChapterTranslatorApi {

    @PostMapping
    @Operation(summary = "Create a new chapter draft (Initialize Google Doc)")
    ResponseEntity<ChapterResponse> create(UserEntity user, ChapterRequest request);

    @PostMapping("/{id}/sync")
    @Operation(summary = "Sync content from Google Doc and Publish")
    ResponseEntity<ChapterResponse> syncAndPublish(@PathVariable UUID id, UserEntity user);

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update chapter status (e.g., Draft <-> Ready)")
    ResponseEntity<ChapterResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateChapterStatusRequest request,
            @AuthenticationPrincipal UserEntity user
    );

}
