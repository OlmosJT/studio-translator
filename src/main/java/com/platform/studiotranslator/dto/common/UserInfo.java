package com.platform.studiotranslator.dto.common;

import com.platform.studiotranslator.constant.Role;

import java.util.UUID;

public record UserInfo(
        UUID userId,
        String displayName,
        String firstName,
        String lastName,
        Role role
) {}
