package ftn.siit.nvt.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIOException(IOException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Error processing file upload: " + ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(ConcurrentModificationException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentModification(
            ConcurrentModificationException ex,
            WebRequest request
    ) {
        log.warn("Concurrent modification detected: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(409)
                .error("CONCURRENT_MODIFICATION")
                .message("The resource was modified by another user. " +
                        "Please refresh and try again.")
                .details(Map.of(
                        "entity", ex.getEntityName(),
                        "entityId", ex.getEntityId(),
                        "expectedVersion", ex.getExpectedVersion(),
                        "currentVersion", ex.getCurrentVersion()
                ))
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(
            ObjectOptimisticLockingFailureException ex,
            WebRequest request
    ) {
        //log.warn("🚨 OPTIMISTIC LOCKING FAILURE!");
        //log.warn("   Message: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(409)
                .error("CONCURRENT_MODIFICATION")
                .message("The resource was modified by another user. " +
                        "Please refresh the page and try again.")
                .details(Map.of(
                        "cause", "Hibernate optimistic locking failure"
                ))
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request
    ) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(404)
                .error("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        log.warn("Validation error: {}", ex.getMessage());

        java.util.List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();

        Map<String, String> fieldErrors = new HashMap<>();
        String firstErrorMessage = null;
        for (ObjectError err : allErrors) {
            if (firstErrorMessage == null) {
                firstErrorMessage = err.getDefaultMessage();
            }
            if (err instanceof FieldError) {
                String fieldName = ((FieldError) err).getField();
                fieldErrors.put(fieldName, err.getDefaultMessage());
            } else {
                fieldErrors.put(err.getObjectName(), err.getDefaultMessage());
            }
        }

        if (firstErrorMessage == null) {
            firstErrorMessage = "Validation failed for request parameters";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("VALIDATION_ERROR")
                .message(firstErrorMessage)
                .details(fieldErrors)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest request
    ) {
        log.warn(" Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity violation. " +
                "This might be due to duplicate entry or constraint violation.";
        if (ex.getMessage() != null && ex.getMessage().contains("unique")) {
            message = "This record already exists. Please use a unique value.";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(409)
                .error("DATA_INTEGRITY_VIOLATION")
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request
    ) {
        log.warn(" Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(403)
                .error("ACCESS_DENIED")
                .message("You do not have permission to access this resource.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ftn.siit.nvt.exception.InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            ftn.siit.nvt.exception.InvalidCredentialsException ex,
            WebRequest request
    ) {
        log.warn("Invalid credentials: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(401)
                .error("INVALID_CREDENTIALS")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ftn.siit.nvt.exception.AccountNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotVerified(
            ftn.siit.nvt.exception.AccountNotVerifiedException ex,
            WebRequest request
    ) {
        log.warn("Account not verified: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(403)
                .error("ACCOUNT_NOT_VERIFIED")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ftn.siit.nvt.exception.AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(
            ftn.siit.nvt.exception.AccountLockedException ex,
            WebRequest request
    ) {
        log.warn("Account locked: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(403)
                .error("ACCOUNT_LOCKED")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request
    ) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(500)
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}