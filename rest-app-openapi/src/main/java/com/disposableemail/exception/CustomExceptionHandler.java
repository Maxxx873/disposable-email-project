package com.disposableemail.exception;

import com.disposableemail.rest.model.ErrorResponse;
import com.mongodb.MongoWriteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.NotAuthorizedException;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(NotAuthorizedException.class)
    public final ResponseEntity<ErrorResponse> handleNotAuthorizedException(NotAuthorizedException ex) {
        log.error("Exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(String.valueOf(HttpStatus.UNAUTHORIZED.value()), "This account is not registered");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccountAlreadyRegisteredException.class)
    public final ResponseEntity<ErrorResponse> handleAccountAlreadyRegisteredException(AccountAlreadyRegisteredException ex) {
        log.error("Exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(String.valueOf(HttpStatus.CONFLICT.value()), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MongoWriteException.class)
    public final ResponseEntity<ErrorResponse> handleMongoWriteException(MongoWriteException ex) {
        log.error("Exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(String.valueOf(HttpStatus.CONFLICT.value()), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public final ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex) {
        log.error("Exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(String.valueOf(HttpStatus.NOT_FOUND.value()), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DomainNotAvailableException.class)
    public final ResponseEntity<ErrorResponse> handleDomainNotFoundException(DomainNotAvailableException ex) {
        log.error("Exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(String.valueOf(HttpStatus.BAD_REQUEST.value()), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(String.valueOf(HttpStatus.BAD_REQUEST.value()), "Bad request, something wrong");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
