package com.platform.studiotranslator.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "ID Token is required")
        String idToken
) {
}
