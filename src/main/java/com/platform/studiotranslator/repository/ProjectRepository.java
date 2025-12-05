package com.platform.studiotranslator.repository;

import com.platform.studiotranslator.constant.ProjectStatus;
import com.platform.studiotranslator.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    // The most used query in the application: "Get book by URL"
    boolean existsBySlug(String slug);
    Optional<ProjectEntity> findBySlug(String slug);

    Page<ProjectEntity> findByStatus(ProjectStatus status, Pageable pageable);

    Page<ProjectEntity> findByTranslatorId(UUID translatorId, Pageable pageable);

    Page<ProjectEntity> findByTranslatorIdAndStatus(UUID translatorId, ProjectStatus status, Pageable pageable);

}
