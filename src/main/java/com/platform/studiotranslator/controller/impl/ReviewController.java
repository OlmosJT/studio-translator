package com.platform.studiotranslator.controller.impl;

import com.platform.studiotranslator.dto.review.ReviewRequest;
import com.platform.studiotranslator.entity.UserEntity;
import com.platform.studiotranslator.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "Ratings and Reviews for Projects")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{projectId}")
    @Operation(summary = "Post or Update a review for a project")
    public ResponseEntity<Void> postReview(
            @PathVariable UUID projectId,
            @RequestBody @Valid ReviewRequest request,
            @AuthenticationPrincipal UserEntity user) {

        reviewService.addOrUpdateReview(user, projectId, request.rating(), request.content());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete your review")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserEntity user) {

        reviewService.deleteReview(user, projectId);
        return ResponseEntity.ok().build();
    }

    // Note: GET endpoint usually returns a Page<ReviewResponseDTO>
}
