package eu.darwin.node.controller;

import eu.darwin.node.dto.ErrorDTO;
import eu.darwin.node.exceptions.ExecutionEngineExeception;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ErrResponseHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(value = {ExecutionEngineExeception.class})
    protected ResponseEntity<Object> handleGenericException(RuntimeException ex, WebRequest request) {
        var resp = new ErrorDTO(ex.getMessage());
        return handleExceptionInternal(ex, resp, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

}
