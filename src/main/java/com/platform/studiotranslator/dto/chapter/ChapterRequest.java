package com.platform.studiotranslator.dto.chapter;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChapterRequest(
        @NotNull(message = "Project ID is required")
        UUID projectId,

        @NotNull(message = "Chapter number is required")
        @DecimalMin(value = "0.0", message = "Chapter number cannot be negative")
        Double chapterNumber,

        @NotBlank(message = "Title is required")
        String title
) {
}
