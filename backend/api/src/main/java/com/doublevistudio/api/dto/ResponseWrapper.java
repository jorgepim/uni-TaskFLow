package com.doublevistudio.api.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ResponseWrapper<T> {
    private String status;
    private String message;
    private Instant timestamp;
    private T data;
}

