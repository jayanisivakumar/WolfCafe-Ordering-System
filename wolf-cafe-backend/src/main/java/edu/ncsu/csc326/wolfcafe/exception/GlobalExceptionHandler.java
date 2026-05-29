package edu.ncsu.csc326.wolfcafe.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Handles global errors.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles global API exceptions
     *
     * @param exception
     *            a WolfCafeAPI exception
     * @param webRequest
     *            the request that caused the exception
     * @return a ResponseEntity encapsulating the exception information for
     *         presentation to the front end
     */
    @ExceptionHandler ( WolfCafeAPIException.class )
    public ResponseEntity<ErrorDetails> handleAPIException ( WolfCafeAPIException exception, WebRequest webRequest ) {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setTimeStamp( LocalDateTime.now() );
        errorDetails.setMessage( exception.getMessage() );
        errorDetails.setDetails( webRequest.getDescription( false ) );

        return new ResponseEntity<>( errorDetails, HttpStatus.BAD_REQUEST );
    }

    /**
     * Handles user not found exceptions for user management.
     */
    @ExceptionHandler ( UserNotFoundException.class )
    public ResponseEntity<Map<String, Object>> handleUserNotFound ( UserNotFoundException ex ) {
        Map<String, Object> body = new HashMap<>();
        body.put( "timestamp", LocalDateTime.now() );
        body.put( "message", ex.getMessage() );
        return new ResponseEntity<>( body, HttpStatus.NOT_FOUND );
    }

    /**
     * Handles validation errors for user management.
     */
    @ExceptionHandler ( MethodArgumentNotValidException.class )
    public ResponseEntity<Map<String, Object>> handleValidationException ( MethodArgumentNotValidException ex ) {
        Map<String, Object> body = new HashMap<>();
        body.put( "timestamp", LocalDateTime.now() );
        body.put( "message", "Validation failed" );
        body.put( "errors", ex.getBindingResult().getFieldErrors().stream()
                .map( error -> error.getField() + ": " + error.getDefaultMessage() ).toArray() );
        return new ResponseEntity<>( body, HttpStatus.BAD_REQUEST );
    }
}
