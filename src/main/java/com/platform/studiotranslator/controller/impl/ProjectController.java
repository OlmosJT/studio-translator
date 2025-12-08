package com.platform.studiotranslator.controller.impl;

import com.platform.studiotranslator.constant.ProjectStatus;
import com.platform.studiotranslator.controller.ProjectPublicApi;
import com.platform.studiotranslator.controller.ProjectTranslatorApi;
import com.platform.studiotranslator.dto.project.ProjectRequest;
import com.platform.studiotranslator.dto.project.ProjectResponse;
import com.platform.studiotranslator.entity.UserEntity;
import com.platform.studiotranslator.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProjectController implements ProjectPublicApi, ProjectTranslatorApi {

    private final ProjectService projectService;


    // --- TRANSLATOR API IMPLEMENTATION ---

    @Override
    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    public ResponseEntity<ProjectResponse> create(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody @Valid ProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.createProject(user, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserEntity user,
            @RequestBody @Valid ProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(id, user, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('TRANSLATOR', 'ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserEntity user
    ) {
        projectService.deleteProject(id, user);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_TRANSLATOR')")
    public ResponseEntity<Page<ProjectResponse>> getMyProjects(
            @AuthenticationPrincipal UserEntity user,
            @RequestParam(required = false) ProjectStatus status,
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // Log to verify user is injected
        if (user == null) throw new RuntimeException("User principal is null in /mine endpoint!");

        UUID myTranslatorId = user.getTranslatorProfile().getId();
        return ResponseEntity.ok(projectService.filterProjects(status, myTranslatorId, pageable));
    }

    // --- PUBLIC API IMPLEMENTATION ---

    @Override
    public ResponseEntity<ProjectResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(projectService.getProjectBySlug(slug));
    }

    @Override
    public ResponseEntity<Page<ProjectResponse>> getAll(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) UUID translatorId,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(projectService.filterProjects(status, translatorId, pageable));
    }
}
