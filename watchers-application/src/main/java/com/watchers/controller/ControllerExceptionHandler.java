package com.watchers.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.exception.NotANumberException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NotANumberException.class)
    public ResponseEntity handleNotANumberException(Exception exception){
        return new ResponseEntity<Object>(
                exception.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

}
