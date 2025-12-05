package com.platform.studiotranslator.dto.project;

import com.platform.studiotranslator.constant.Language;
import com.platform.studiotranslator.constant.ProjectGenre;
import com.platform.studiotranslator.constant.ProjectStatus;
import com.platform.studiotranslator.constant.ProjectType;
import com.platform.studiotranslator.dto.common.UserInfo;

import java.util.Set;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String slug, // SEO Friendly URL
        String title,
        String synopsis,
        String coverImageUrl,
        String sourceLink,
        String originalAuthor,
        Language originalLanguage,
        Language targetLanguage,
        ProjectType type,
        ProjectStatus status,
        Set<ProjectGenre> genres,
        Integer chapterCount,
        Long viewCount,
        Double averageRating,
        Integer totalReviews,
        UserInfo translator
) {

}
