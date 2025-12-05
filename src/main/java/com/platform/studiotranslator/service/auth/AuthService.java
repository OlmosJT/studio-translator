package com.platform.studiotranslator.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.platform.studiotranslator.constant.AuthProvider;
import com.platform.studiotranslator.constant.Role;
import com.platform.studiotranslator.dto.auth.*;
import com.platform.studiotranslator.dto.common.UserInfo;
import com.platform.studiotranslator.entity.UserEntity;
import com.platform.studiotranslator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifier googleVerifier;

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse authenticateGoogle(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = googleVerifier.verify(request.idToken());
        String email = payload.getEmail();

        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> registerGoogleUser(payload));

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use.");
        }
        if (userRepository.existsByDisplayName(request.displayName())) {
            throw new IllegalArgumentException("Display name is already taken.");
        }

        UserEntity newUser = UserEntity.builder()
                .displayName(request.displayName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .authProvider(AuthProvider.LOCAL)
                .role(Role.READER)
                .build();

        userRepository.save(newUser);

        return buildAuthResponse(newUser);
    }

    private UserEntity registerGoogleUser(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        String googleSub = payload.getSubject();
        String pictureUrl = (String) payload.get("picture");
        String fullName = (String) payload.get("name");

        String displayName = fullName.replaceAll("\\s+", "").toLowerCase();

        if (userRepository.existsByDisplayName(displayName)) {
            displayName = displayName + UUID.randomUUID().toString().substring(0, 4);
        }

        UserEntity newUser = UserEntity.builder()
                .email(email)
                .displayName(displayName)
                .googleSub(googleSub)
                .authProvider(AuthProvider.GOOGLE)
                .role(Role.READER)
                .avatarUrl(pictureUrl)
                .build();

        return userRepository.save(newUser);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String userEmail = jwtService.extractUsername(request.token());

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (jwtService.isTokenValid(request.token(), user)) {
            return buildAuthResponse(user);
        }

        throw new IllegalArgumentException("Invalid refresh token");
    }

    private AuthResponse buildAuthResponse(UserEntity user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        String firstName = null;
        String lastName = null;

        if (user.getTranslatorProfile() != null) {
            firstName = user.getTranslatorProfile().getFirstName();
            lastName = user.getTranslatorProfile().getLastName();
        }

        var userInfo = new UserInfo(
                user.getId(),
                user.getDisplayName(),
                firstName,
                lastName,
                user.getRole()
        );

        return new AuthResponse(accessToken, refreshToken, userInfo);
    }
}
