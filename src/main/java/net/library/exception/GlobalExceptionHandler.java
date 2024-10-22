package net.library.exception;

import lombok.extern.slf4j.Slf4j;
import net.library.model.response.HttpErrorResponse;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static net.library.exception.ErrorId.*;
import static net.library.exception.ErrorMessage.*;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<HttpErrorResponse> duplicateEntry(ConstraintViolationException ex) {
        return httpErrorResponseBuilder(ex, USER_ALREADY_EXISTS_ERROR_ID, USER_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<HttpErrorResponse> badRequestValidation(MethodArgumentNotValidException ex) {
        return httpErrorResponseBuilder(ex, BAD_REQUEST_ERROR_ID, MISSING_PARAM, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpErrorResponse> defaultError(Exception ex) {
        return httpErrorResponseBuilder(ex, DEFAULT_ERROR_ID, DEFAULT_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FilterLengthException.class)
    public ResponseEntity<HttpErrorResponse> filterLength(Exception ex) {
        return httpErrorResponseBuilder(ex, FILTER_ID, FILTER_LENGTH, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<HttpErrorResponse> userAlreadyExists(Exception ex) {
        return httpErrorResponseBuilder(ex, USER_ALREADY_EXISTS_ERROR_ID, USER_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WrongMsisdnException.class)
    public ResponseEntity<HttpErrorResponse> wrongMsisdnFormat(Exception ex) {
        return httpErrorResponseBuilder(ex, WRONG_MSISDN_FORMAT_ERROR_ID, WRONG_MSISDN_FORMAT, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<HttpErrorResponse> wrongTypeFormat(Exception ex) {
        return httpErrorResponseBuilder(ex, 110, "wrong type format", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WrongState.class)
    public ResponseEntity<HttpErrorResponse> wrongState(Exception ex) {
        return httpErrorResponseBuilder(ex, WRONG_STATE_ID, WRONG_STATE, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpErrorResponse> userNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<HttpErrorResponse> missingParam(Exception ex) {
        return httpErrorResponseBuilder(ex, WRONG_STATE_ID, WRONG_STATE, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<HttpErrorResponse> httpErrorResponseBuilder(Exception ex, int errorCode, String errorMsg, HttpStatus code) {
        var cid = MdcUtils.getCid();
        log.error("Error: {}, cid {}", ex, cid);

        return new ResponseEntity<>(new HttpErrorResponse(cid, errorCode,
                errorMsg), code);
    }
}