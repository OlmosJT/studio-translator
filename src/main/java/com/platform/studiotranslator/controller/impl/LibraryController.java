package com.platform.studiotranslator.controller.impl;

import com.platform.studiotranslator.constant.ReadingStatus;
import com.platform.studiotranslator.entity.UserEntity;
import com.platform.studiotranslator.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
@Tag(name = "Library API", description = "Manage user's reading list")
public class LibraryController {

    private final LibraryService libraryService;

    @PostMapping("/{projectId}")
    @Operation(summary = "Add book to library or update status")
    public ResponseEntity<Void> addToLibrary(
            @PathVariable UUID projectId,
            @RequestParam ReadingStatus status,
            @AuthenticationPrincipal UserEntity user) {

        libraryService.addOrUpdateLibraryEntry(user, projectId, status);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{projectId}/progress/{chapterId}")
    @Operation(summary = "Update reading progress (Last Read Chapter)")
    public ResponseEntity<Void> updateProgress(
            @PathVariable UUID projectId,
            @PathVariable UUID chapterId,
            @AuthenticationPrincipal UserEntity user) {

        libraryService.updateProgress(user, projectId, chapterId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Remove book from library (Soft Delete)")
    public ResponseEntity<Void> removeFromLibrary(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserEntity user) {

        libraryService.removeFromLibrary(user, projectId);
        return ResponseEntity.ok().build();
    }

    // NOTE: You'll likely need a GET endpoint returning a DTO list here later.
}
