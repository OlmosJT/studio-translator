package com.platform.studiotranslator.config.filter;

import com.platform.studiotranslator.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        int cacheLimit = 500_000; // 500 KB safe limit
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request, cacheLimit);

        String requestId = UUID.randomUUID().toString().substring(0, 8);

        logCompactRequest(req, requestId);

        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("[{}] No JWT token in Authorization header", requestId);
            filterChain.doFilter(req, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            String email = jwtService.extractUsername(jwt);

            log.info("[{}] JWT -> user={}", requestId, email);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    log.info("[{}] Authenticated -> {}", requestId, email);
                } else {
                    log.warn("[{}] Invalid JWT for {}", requestId, email);
                }

            }

        } catch (Exception ex) {
            log.error("[{}] JWT error: {}", requestId, ex.getMessage());
        }

        filterChain.doFilter(req, response);
    }

    /**
     * Compact request logging block (just enough info).
     */
    private void logCompactRequest(ContentCachingRequestWrapper req, String id) {

        String body = "";
        try {
            body = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {}

        if (body.length() > 300) {
            body = body.substring(0, 300) + "...(truncated)";
        }

        String headers = Collections.list(req.getHeaderNames()).stream()
                .map(h -> h + "=" + req.getHeader(h))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        if (headers.length() > 300) {
            headers = headers.substring(0, 300) + "...";
        }

        log.info("\n" +
                        "┌────────── JWT FILTER [{}] ───────────┐\n" +
                        "│ {} {}\n" +
                        "│ Headers: {}\n" +
                        "│ Body: {}\n" +
                        "└──────────────────────────────────────┘",
                id,
                req.getMethod(), req.getRequestURI(),
                headers,
                (body.isBlank() ? "<empty>" : body)
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}
