package com.platform.studiotranslator.mapper;

import com.platform.studiotranslator.dto.chapter.ChapterResponse;
import com.platform.studiotranslator.entity.ChapterEntity;
import org.springframework.stereotype.Component;

@Component
public class ChapterMapper {

    public ChapterResponse toResponse(ChapterEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ChapterResponse(
                entity.getId(),
                entity.getChapterNumber(),
                entity.getTitle(),
                entity.getContent(),
                entity.getWordCount(),
                entity.getViewCount(),
                entity.getUpdatedAt(),
                entity.getStatus(),
                entity.getGoogleDocUrl(),
                entity.getLastSyncedAt()
        );
    }
}
