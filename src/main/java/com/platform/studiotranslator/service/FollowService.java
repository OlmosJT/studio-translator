package com.platform.studiotranslator.service;

import com.platform.studiotranslator.entity.FollowEntity;
import com.platform.studiotranslator.entity.TranslatorEntity;
import com.platform.studiotranslator.entity.UserEntity;
import com.platform.studiotranslator.repository.FollowRepository;
import com.platform.studiotranslator.repository.TranslatorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final TranslatorRepository translatorRepository;

    @Transactional
    public void followTranslator(UserEntity follower, UUID translatorId) {
        // 1. Self-Follow Check
        // Since TranslatorEntity shares the same UUID as UserEntity (@MapsId),
        // we can simply compare the IDs.
        if (follower.getId().equals(translatorId)) {
            throw new IllegalArgumentException("You cannot follow yourself.");
        }

        // 2. Check for existing relationship (Active OR Deleted)
        var existingFollow = followRepository.findRawByFollowerAndFollowed(follower.getId(), translatorId);

        if (existingFollow.isPresent()) {
            FollowEntity follow = existingFollow.get();

            if (follow.getDeletedAt() == null) {
                // Already following actively - ignore
                return;
            } else {
                // RESTORE: The relationship existed but was deleted. Un-delete it.
                follow.setDeletedAt(null);
                follow.setReceiveNotifications(true); // Reset to default
                followRepository.save(follow);

                // Update stats
                incrementFollowerCount(translatorId);
                return;
            }
        }

        // 3. Create NEW Follow
        TranslatorEntity translator = translatorRepository.findById(translatorId)
                .orElseThrow(() -> new EntityNotFoundException("Translator not found"));

        FollowEntity newFollow = FollowEntity.builder()
                .follower(follower)
                .followed(translator)
                .receiveNotifications(true)
                .build();

        followRepository.save(newFollow);
        incrementFollowerCount(translatorId);
    }

    @Transactional
    public void unfollowTranslator(UserEntity follower, UUID translatorId) {
        FollowEntity follow = followRepository.findByFollowerIdAndFollowedId(follower.getId(), translatorId)
                .orElseThrow(() -> new EntityNotFoundException("You are not following this translator"));

        // Soft delete via @SQLDelete
        followRepository.delete(follow);

        decrementFollowerCount(translatorId);
    }

    // --- Helper Methods for Stats ---

    private void incrementFollowerCount(UUID translatorId) {
        // Direct DB update: Atomic and Fast
        translatorRepository.incrementFollowerCount(translatorId);
    }

    private void decrementFollowerCount(UUID translatorId) {
        // Direct DB update
        translatorRepository.decrementFollowerCount(translatorId);
    }
}
