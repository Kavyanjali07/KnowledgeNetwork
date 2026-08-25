package com.knowledgenetwork.common.exception;

import com.knowledgenetwork.common.dto.ErrorDetails;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ErrorDetails> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .type("https://knowledgenetwork.io/errors/unauthorized")
                .title("Unauthorized Access")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex instanceof BadCredentialsException ? "Invalid email or password provided." : "Authentication token is missing or invalid.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorDetails> handleDisabledException(
            DisabledException ex, HttpServletRequest request) {
        log.warn("Authentication failed: user account is disabled");

        ErrorDetails error = ErrorDetails.builder()
                .type("https://knowledgenetwork.io/errors/account-disabled")
                .title("Account Disabled")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("User account has been disabled. Please contact administrator.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorDetails> handleEmailNotVerifiedException(
            EmailNotVerifiedException ex, HttpServletRequest request) {
        log.warn("Email verification required: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .type("https://knowledgenetwork.io/errors/email-not-verified")
                .title("Email Not Verified")
                .status(HttpStatus.FORBIDDEN.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "EMAIL_NOT_VERIFIED: Please verify your email before signing in.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for URI {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .type("https://knowledgenetwork.io/errors/forbidden")
                .title("Access Denied")
                .status(HttpStatus.FORBIDDEN.value())
                .detail("You do not have permission to access this resource.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .type("https://knowledgenetwork.io/errors/not-found")
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({BusinessException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorDetails> handleBadRequestExceptions(
            Exception ex, HttpServletRequest request) {
        log.warn("Bad request: {}", ex.getMessage());

        String message = ex.getMessage();
        if (ex instanceof HttpMessageNotReadableException) {
            message = "Malformed or unreadable HTTP request payload.";
        }

        ErrorDetails error = ErrorDetails.builder()
                .type("https://knowledgenetwork.io/errors/bad-request")
                .title("Bad Request")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(message)
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ConcurrencyConflictException.class, ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ErrorDetails> handleConcurrencyConflictException(
            Exception ex, HttpServletRequest request) {
        log.warn("Optimistic locking conflict detected: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .type("https://knowledgenetwork.io/errors/concurrency-conflict")
                .title("Concurrency Conflict")
                .status(HttpStatus.CONFLICT.value())
                .detail("The entity was updated or deleted by another user concurrently. Please reload and try again.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ErrorDetails> handleUnprocessableEntityException(
            UnprocessableEntityException ex, HttpServletRequest request) {
        log.warn("Unprocessable entity: {}", ex.getMessage());

        ErrorDetails error = ErrorDetails.builder()
                .type("https://knowledgenetwork.io/errors/unprocessable-entity")
                .title("Unprocessable Entity")
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorDetails> handleValidationExceptions(
            Exception ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (ex instanceof MethodArgumentNotValidException validEx) {
            validEx.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        } else if (ex instanceof ConstraintViolationException constraintEx) {
            constraintEx.getConstraintViolations().forEach(violation -> {
                String fieldName = violation.getPropertyPath().toString();
                String errorMessage = violation.getMessage();
                errors.put(fieldName, errorMessage);
            });
        }

        ErrorDetails errorDetails = ErrorDetails.builder()
                .type("https://knowledgenetwork.io/errors/validation-error")
                .title("Validation Failed")
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .detail("One or more request parameters failed validation constraints.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .validationErrors(errors)
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled server exception encountered", ex);

        ErrorDetails errorDetails = ErrorDetails.builder()
                .type("https://knowledgenetwork.io/errors/internal-server-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred. Please contact system support.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
