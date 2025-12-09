package com.platform.studiotranslator.controller.impl;

import com.platform.studiotranslator.entity.UserEntity;
import com.platform.studiotranslator.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Tag(name = "Follow API", description = "Endpoints for following translators")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{translatorId}")
    @Operation(summary = "Follow a translator")
    public ResponseEntity<Void> follow(@PathVariable UUID translatorId,
                                       @AuthenticationPrincipal UserEntity user) {
        followService.followTranslator(user, translatorId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{translatorId}")
    @Operation(summary = "Unfollow a translator")
    public ResponseEntity<Void> unfollow(@PathVariable UUID translatorId,
                                         @AuthenticationPrincipal UserEntity user) {
        followService.unfollowTranslator(user, translatorId);
        return ResponseEntity.ok().build();
    }
}
