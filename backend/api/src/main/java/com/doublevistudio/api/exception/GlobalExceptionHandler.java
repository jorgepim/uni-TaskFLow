package com.doublevistudio.api.exception;

import com.doublevistudio.api.dto.ResponseWrapper;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ResponseWrapper<Object> wrap = new ResponseWrapper<>();
        wrap.setStatus("error");
        wrap.setMessage(msg);
        wrap.setTimestamp(Instant.now());
        wrap.setData(null);
        return new ResponseEntity<>(wrap, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));
        ResponseWrapper<Object> wrap = new ResponseWrapper<>();
        wrap.setStatus("error");
        wrap.setMessage(msg);
        wrap.setTimestamp(Instant.now());
        wrap.setData(null);
        return new ResponseEntity<>(wrap, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String detail = ex.getMessage() != null ? ex.getMessage() : "";
        ResponseWrapper<Object> wrap = new ResponseWrapper<>();
        wrap.setStatus("error");
        wrap.setMessage("JSON inválido o campo faltante: " + detail);
        wrap.setTimestamp(Instant.now());
        wrap.setData(null);
        return new ResponseEntity<>(wrap, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Object>> handleAll(Exception ex) {
        ResponseWrapper<Object> wrap = new ResponseWrapper<>();
        wrap.setStatus("error");
        wrap.setMessage("Error interno: " + ex.getMessage());
        wrap.setTimestamp(Instant.now());
        wrap.setData(null);
        return new ResponseEntity<>(wrap, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
