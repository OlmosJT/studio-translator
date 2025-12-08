package com.platform.studiotranslator.controller;

import com.platform.studiotranslator.constant.ProjectStatus;
import com.platform.studiotranslator.dto.project.ProjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(
        name = "Project Public API",
        description = "Endpoints accessible to all users (Readers)"
)
@RequestMapping("/api/projects")
public interface ProjectPublicApi {

    @GetMapping("/{slug}")
    @Operation(summary = "Get project details by slug")
    ResponseEntity<ProjectResponse> getBySlug(@PathVariable String slug);

    @GetMapping
    @Operation(summary = "Browse all projects")
    ResponseEntity<Page<ProjectResponse>> getAll(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) UUID translatorId,
            Pageable pageable
    );
}
