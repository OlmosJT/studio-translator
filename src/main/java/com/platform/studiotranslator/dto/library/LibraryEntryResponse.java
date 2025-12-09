package com.platform.studiotranslator.dto.library;

import com.platform.studiotranslator.constant.ReadingStatus;

import java.util.UUID;

public record LibraryEntryResponse(
        UUID id,
        UUID projectId,
        String projectTitle,
        String projectCoverUrl,
        ReadingStatus status,
        UUID lastReadChapterId,
        Double lastReadChapterNumber, // "Ch 15.5"
        String lastReadChapterTitle
) {}
