package com.disposableemail.core.exception;

import com.disposableemail.core.exception.custom.*;
import com.disposableemail.core.model.ErrorResponse;
import com.mongodb.MongoWriteException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;


@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String EXCEPTION_LOG = "Exception: {}";

    @ExceptionHandler(NotAuthorizedException.class)
    public final ResponseEntity<ErrorResponse> handleNotAuthorizedException(NotAuthorizedException ex) {
        log.error(EXCEPTION_LOG, ex.getMessage());
        var error = new ErrorResponse(String.valueOf(HttpStatus.UNAUTHORIZED.value()), "Not registered Account");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ErrorResponse> handleNotAuthorizedException(AccessDeniedException ex) {
        log.error(EXCEPTION_LOG, ex.getMessage());
        var error = new ErrorResponse(String.valueOf(HttpStatus.UNAUTHORIZED.value()), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccountToManyRequestsException.class)
    public final ResponseEntity<ErrorResponse> handleAccountToManyRequestException(AccountToManyRequestsException ex) {
        log.error(EXCEPTION_LOG, ex.getMessage());
        var error = new ErrorResponse(String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler({AccountAlreadyRegisteredException.class, MongoWriteException.class})
    public final ResponseEntity<ErrorResponse> handleAccountAlreadyRegisteredException(AccountAlreadyRegisteredException ex) {
        log.error(EXCEPTION_LOG, ex.getMessage());
        var error = new ErrorResponse(String.valueOf(HttpStatus.CONFLICT.value()), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({AccountNotFoundException.class, MessageNotFoundException.class, SourceNotFoundException.class,
            MessagesNotFoundException.class, DomainsNotFoundException.class, DomainNotFoundException.class})
    public final ResponseEntity<ErrorResponse> handleAccountNotFoundException(Exception ex) {
        log.error(EXCEPTION_LOG, ex.getMessage());
        var error = new ErrorResponse(String.valueOf(HttpStatus.NOT_FOUND.value()), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DomainNotAvailableException.class)
    public final ResponseEntity<ErrorResponse> handleDomainNotAvailableException(DomainNotAvailableException ex) {
        log.error(EXCEPTION_LOG, ex.getMessage());
        var error = new ErrorResponse(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error(EXCEPTION_LOG, ex.getMessage());
        var error = new ErrorResponse(String.valueOf(HttpStatus.BAD_REQUEST.value()), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error(EXCEPTION_LOG, ex.getMessage());
        ex.printStackTrace();
        var error = new ErrorResponse(String.valueOf(HttpStatus.BAD_REQUEST.value()), "Bad request, something wrong");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
