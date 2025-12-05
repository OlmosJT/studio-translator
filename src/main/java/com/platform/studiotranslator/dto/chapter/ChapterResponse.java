package com.platform.studiotranslator.dto.chapter;

import com.platform.studiotranslator.constant.ChapterStatus;

import java.time.Instant;
import java.util.UUID;

public record ChapterResponse(
        UUID id,
        Double chapterNumber,
        String title,
        String content,     // Null if DRAFT (reader can't see)
        Integer wordCount,
        Long viewCount,
        Instant updatedAt,
        ChapterStatus status,
        String googleDocUrl, // Only visible to Translator/Admin
        Instant lastSyncedAt
) {
}
