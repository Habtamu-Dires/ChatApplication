package com.example.app.exception;

import com.example.app.api_response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleResourceNotFoundException(
            ResourceNotFoundException ex
    ) {
        ApiResponse<String> response = new ApiResponse<>(
                false, null, ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidRequestException(
            InvalidRequestException ex
    ) {
        ApiResponse<String> response = new ApiResponse<>(
                false,null,ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(RuntimeException ex) {
        ApiResponse<String> response = new ApiResponse<>(
                false, null, "An unexpected error has occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
