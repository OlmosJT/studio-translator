package com.platform.studiotranslator.exception;

public class GoogleEmailNotVerifiedException extends RuntimeException {
    public GoogleEmailNotVerifiedException() {
        super("Google account email is not verified.");
    }
}
