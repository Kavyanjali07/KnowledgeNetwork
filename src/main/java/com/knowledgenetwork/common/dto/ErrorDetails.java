package com.knowledgenetwork.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {

    private final String type;
    private final String title;
    private final int status;
    private final String detail;
    private final String instance;
    private final Instant timestamp;
    private final Map<String, String> validationErrors;

    public ErrorDetails(String type, String title, int status, String detail, String instance, Instant timestamp, Map<String, String> validationErrors) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.validationErrors = validationErrors;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public String getInstance() {
        return instance;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type;
        private String title;
        private int status;
        private String detail;
        private String instance;
        private Instant timestamp = Instant.now();
        private Map<String, String> validationErrors;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder validationErrors(Map<String, String> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }

        public ErrorDetails build() {
            return new ErrorDetails(type, title, status, detail, instance, timestamp, validationErrors);
        }
    }
}
