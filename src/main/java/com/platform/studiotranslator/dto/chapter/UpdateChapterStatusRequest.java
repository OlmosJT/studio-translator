package com.platform.studiotranslator.dto.chapter;

import com.platform.studiotranslator.constant.ChapterStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateChapterStatusRequest(
        @NotNull(message = "Status is required")
        ChapterStatus status
) {}
