package com.knowledgenetwork.common.exception;

public class EmailNotVerifiedException extends RuntimeException {

    private final String email;

    public EmailNotVerifiedException(String message) {
        super(message);
        this.email = null;
    }

    public EmailNotVerifiedException(String email, String message) {
        super(message);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
