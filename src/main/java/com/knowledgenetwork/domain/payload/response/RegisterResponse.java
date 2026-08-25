package com.knowledgenetwork.domain.payload.response;

public class RegisterResponse {

    private String email;
    private boolean verificationRequired = true;
    private String message;

    public RegisterResponse() {
    }

    public RegisterResponse(String email, boolean verificationRequired, String message) {
        this.email = email;
        this.verificationRequired = verificationRequired;
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerificationRequired() {
        return verificationRequired;
    }

    public void setVerificationRequired(boolean verificationRequired) {
        this.verificationRequired = verificationRequired;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
