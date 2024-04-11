package com.antarctic.explorer.api.controller;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
      ConstraintViolationException e) {

    List<SubError> subErrors =
        e.getConstraintViolations().stream()
            .map(
                error -> {
                  String name = null;
                  for (Path.Node node : error.getPropertyPath()) name = node.getName();

                  return new SubError(name, error.getInvalidValue(), error.getMessage());
                })
            .collect(Collectors.toList());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ValidationErrorResponse(400, "Validation Error", subErrors));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ValidationErrorResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex) {

    String message;

    if (ex.getParameter().hasParameterAnnotation(DateTimeFormat.class)) {
      DateTimeFormat dateTimeFormat =
          ex.getParameter().getParameterAnnotation(DateTimeFormat.class);
      assert dateTimeFormat != null;

      message = "must be a Date with the format " + dateTimeFormat.pattern();
    } else message = "must be a " + ex.getParameter().getNestedParameterType();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ValidationErrorResponse(
                400,
                "Validation Error",
                Collections.singletonList(new SubError(ex.getName(), ex.getValue(), message))));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(400, "Request method '" + ex.getMethod() + "' is not supported"));
  }

  @ExceptionHandler({NoHandlerFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFoundException(NoHandlerFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
    e.printStackTrace();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
  }

  public record ErrorResponse(int status, String message) {}

  public record ValidationErrorResponse(int status, String message, List<SubError> subErrors) {}

  public record SubError(String field, Object rejectedValue, String message) {}
}
