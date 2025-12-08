package com.platform.studiotranslator.service;

import com.platform.studiotranslator.constant.ProjectStatus;
import com.platform.studiotranslator.constant.Role;
import com.platform.studiotranslator.dto.common.UserInfo;
import com.platform.studiotranslator.dto.project.ProjectRequest;
import com.platform.studiotranslator.dto.project.ProjectResponse;
import com.platform.studiotranslator.entity.ProjectEntity;
import com.platform.studiotranslator.entity.TranslatorEntity;
import com.platform.studiotranslator.entity.UserEntity;
import com.platform.studiotranslator.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    private static final Pattern ALLOWED_SLUG_CHARS = Pattern.compile("[^\\p{L}\\p{N}-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private String generateSlug(String input) {
        if (input == null || input.isEmpty()) return "untitled";
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = ALLOWED_SLUG_CHARS.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    @Transactional
    public ProjectResponse createProject(UserEntity user, ProjectRequest request) {
        TranslatorEntity translator = user.getTranslatorProfile();
        // Defensive check: Controller should handle role, but this is good safety
        if (translator == null) {
            throw new IllegalArgumentException("User is not a translator");
        }

        String baseSlug = generateSlug(request.title());
        String finalSlug = baseSlug;

        // Logic check: What if the title was "!!! ???" -> slug becomes empty ""
        if (finalSlug.isEmpty()) {
            finalSlug = "project-" + UUID.randomUUID().toString().substring(0, 8);
        }

        // Retry Loop for uniqueness
        while (projectRepository.existsBySlug(finalSlug)) {
            finalSlug = baseSlug + "-" + UUID.randomUUID().toString().substring(0, 4);
        }

        ProjectEntity project = ProjectEntity.builder()
                .translator(translator)
                .title(request.title())
                .slug(finalSlug)
                .synopsis(request.synopsis())
                .coverImageUrl(request.coverImageUrl())
                .originalAuthor(request.originalAuthor())
                .sourceLink(request.sourceLink())
                .originalLanguage(request.originalLanguage())
                .targetLanguage(request.targetLanguage())
                .type(request.type())
                .genres(request.genres())
                .status(ProjectStatus.ONGOING)
                .chapterCount(0)
                .viewCount(0L)
                .averageRating(0.0)
                .totalReviews(0)
                .build();

        return mapToResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UserEntity user, ProjectRequest request) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        boolean isOwner = project.getTranslator().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("You do not own this project");
        }

        project.setTitle(request.title());
        project.setSynopsis(request.synopsis());
        project.setCoverImageUrl(request.coverImageUrl());
        project.setOriginalAuthor(request.originalAuthor());
        project.setSourceLink(request.sourceLink());
        project.setOriginalLanguage(request.originalLanguage());
        project.setTargetLanguage(request.targetLanguage());
        project.setType(request.type());

        project.setGenres(request.genres());

        if (request.status() != null) {
            project.setStatus(request.status());
        }

        return mapToResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse getProjectBySlug(String slug) {
        ProjectEntity project = projectRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        return mapToResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> filterProjects(
            ProjectStatus status,
            UUID translatorId,
            Pageable pageable
    ) {
        Page<ProjectEntity> page;

        if (translatorId != null && status != null) {
            page = projectRepository.findByTranslatorIdAndStatus(translatorId, status, pageable);
        } else if (translatorId != null) {
            page = projectRepository.findByTranslatorId(translatorId, pageable);
        } else if (status != null) {
            page = projectRepository.findByStatus(status, pageable);
        } else {
            page = projectRepository.findAll(pageable);
        }
        return page.map(this::mapToResponse);
    }

    @Transactional
    public void deleteProject(UUID projectId, UserEntity user) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));


        boolean isOwner = project.getTranslator().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("You do not have permission to delete this project");
        }

        // Soft Delete (Triggered by @SQLDelete on Entity)
        projectRepository.delete(project);
    }

    private ProjectResponse mapToResponse(ProjectEntity p) {
        TranslatorEntity t = p.getTranslator();
        UserEntity u = t.getUser();

        UserInfo translatorInfo = new UserInfo(
                u.getId(),
                u.getDisplayName(),
                t.getFirstName(),
                t.getLastName(),
                u.getRole()
        );

        return new ProjectResponse(
                p.getId(),
                p.getSlug(),
                p.getTitle(),
                p.getSynopsis(),
                p.getCoverImageUrl(),
                p.getSourceLink(),
                p.getOriginalAuthor(),
                p.getOriginalLanguage(),
                p.getTargetLanguage(),
                p.getType(),
                p.getStatus(),
                p.getGenres(),
                p.getChapterCount(),
                p.getViewCount(),
                p.getAverageRating(),
                p.getTotalReviews(),
                translatorInfo
        );
    }
}
