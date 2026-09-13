package com.voidkey.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import com.voidkey.backend.form.FormNotFoundException;
import com.voidkey.backend.form.FormAccessDeniedException;
import com.voidkey.backend.form.QuestionNotFoundException;
import com.voidkey.backend.form.InvalidReorderException;
/**
 * Without this class, an unhandled BadCredentialsException (wrong password) or a
 * validation failure would either 500 or return Spring's default, fairly ugly error
 * body. @RestControllerAdvice intercepts exceptions THROWN BY ANY CONTROLLER and lets
 * you shape the response once, centrally, instead of try/catching in every method.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid email or password"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
    @ExceptionHandler(FormNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFormNotFound(FormNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(FormAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleFormAccessDenied(FormAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }
    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleQuestionNotFound(QuestionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidReorderException.class)
    public ResponseEntity<Map<String, String>> handleInvalidReorder(InvalidReorderException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
