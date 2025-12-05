package com.platform.studiotranslator.dto.project;

import com.platform.studiotranslator.constant.Language;
import com.platform.studiotranslator.constant.ProjectGenre;
import com.platform.studiotranslator.constant.ProjectStatus;
import com.platform.studiotranslator.constant.ProjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ProjectRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200)
        String title,

        @NotBlank(message = "Synopsis is required")
        String synopsis,

        String coverImageUrl,

        @NotBlank(message = "Original Author is required")
        String originalAuthor,

        String sourceLink,

        @NotNull(message = "Original Language is required")
        Language originalLanguage,

        @NotNull(message = "Target Language is required")
        Language targetLanguage,

        @NotNull(message = "Project Type is required")
        ProjectType type, // FICTION, TECHNICAL, etc.

        @NotNull(message = "At least one genre is required")
        Set<ProjectGenre> genres,

        // Optional: Only used during updates
        ProjectStatus status
) {
}
