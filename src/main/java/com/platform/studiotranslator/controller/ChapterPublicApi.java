package com.platform.studiotranslator.controller;

import com.platform.studiotranslator.dto.chapter.ChapterResponse;
import com.platform.studiotranslator.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Chapter Public API",
        description = "Reading endpoints accessible to all users"
)
@RequestMapping("/api/chapters")
public interface ChapterPublicApi {
    @GetMapping("/{id}")
    @Operation(summary = "Read a specific chapter content")
    ResponseEntity<ChapterResponse> read(@PathVariable UUID id);

    @GetMapping("/project/{slug}")
    @Operation(summary = "Get Table of Contents for a project")
    ResponseEntity<List<ChapterResponse>> getTableOfContents(
            @PathVariable String slug,
            UserEntity user
    );
}
