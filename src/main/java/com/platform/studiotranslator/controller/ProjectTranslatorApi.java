package com.platform.studiotranslator.controller;

import com.platform.studiotranslator.constant.ProjectStatus;
import com.platform.studiotranslator.dto.project.ProjectRequest;
import com.platform.studiotranslator.dto.project.ProjectResponse;
import com.platform.studiotranslator.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Project Translator API",
        description = "Management endpoints for Translators"
)
@RequestMapping("/api/projects")
@SecurityRequirement(name = "bearerAuth")
public interface ProjectTranslatorApi {

    @PostMapping
    @Operation(summary = "Create a new project")
    ResponseEntity<ProjectResponse> create(UserEntity user, ProjectRequest request);

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing project")
    ResponseEntity<ProjectResponse> update(UUID id, UserEntity user, ProjectRequest request);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    ResponseEntity<Void> delete(UUID id, UserEntity user);

    @GetMapping("/mine")
    @Operation(summary = "Get currently logged-in translator's projects")
    ResponseEntity<Page<ProjectResponse>> getMyProjects(
            UserEntity user,
            ProjectStatus status,
            Pageable pageable
    );
}
