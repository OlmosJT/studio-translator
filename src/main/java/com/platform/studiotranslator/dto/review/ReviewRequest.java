package com.platform.studiotranslator.dto.review;

public record ReviewRequest(
        Short rating, // 1-5
        String content
) {}
