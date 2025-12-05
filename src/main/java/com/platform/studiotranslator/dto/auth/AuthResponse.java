package com.platform.studiotranslator.dto.auth;

import com.platform.studiotranslator.dto.common.UserInfo;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserInfo userInfo
) {


}
