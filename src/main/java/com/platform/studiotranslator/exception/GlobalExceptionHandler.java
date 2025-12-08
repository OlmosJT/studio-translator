package com.platform.studiotranslator.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail; // Spring Boot 3 Feature
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access Denied", "message", "You do not have permission to access this resource."));
    }

    @ExceptionHandler(InvalidGoogleTokenException.class)
    public ProblemDetail handleInvalidGoogleToken(InvalidGoogleTokenException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Invalid Google Token");
        pd.setDetail(ex.getMessage());
        pd.setProperty("errorCode", "GOOGLE_TOKEN_INVALID");
        return pd;
    }

    @ExceptionHandler(GoogleEmailNotVerifiedException.class)
    public ProblemDetail handleGoogleEmailNotVerified(GoogleEmailNotVerifiedException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Email Not Verified");
        pd.setDetail(ex.getMessage());
        pd.setProperty("errorCode", "GOOGLE_EMAIL_NOT_VERIFIED");
        return pd;
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ProblemDetail handleExpiredJwt(ExpiredJwtException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Token Expired");
        pd.setDetail("Token has expired. Please login again.");
        pd.setProperty("errorCode", "JWT_EXPIRED");
        return pd;
    }

    @ExceptionHandler(SignatureException.class)
    public ProblemDetail handleInvalidJwt(SignatureException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Invalid Token Signature");
        pd.setDetail("Invalid token signature.");
        pd.setProperty("errorCode", "JWT_INVALID_SIGNATURE");
        return pd;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Bad Credentials");
        pd.setDetail("Invalid email or password.");
        pd.setProperty("errorCode", "AUTH_BAD_CREDENTIALS");
        return pd;
    }

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabledUser(DisabledException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Account Disabled");
        pd.setDetail("Your account is disabled.");
        pd.setProperty("errorCode", "AUTH_DISABLED");
        return pd;
    }

    @ExceptionHandler(LockedException.class)
    public ProblemDetail handleLockedUser(LockedException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Account Locked");
        pd.setDetail("Your account is locked.");
        pd.setProperty("errorCode", "AUTH_LOCKED");
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid Request");
        pd.setDetail(ex.getMessage());
        pd.setProperty("errorCode", "REQUEST_INVALID");
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ex.printStackTrace();

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal Server Error");
        pd.setDetail("An unexpected error occurred.");
        pd.setProperty("errorCode", "GENERIC_ERROR");

        return pd;
    }
}
