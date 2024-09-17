package net.library.exception;

import net.library.model.response.HttpErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static net.library.exception.ErrorId.*;
import static net.library.exception.ErrorMessage.*;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<HttpErrorResponse> badRequestValidation(MethodArgumentNotValidException ex) {
        var cid = MdcUtils.getCid();
        LOGGER.error("Error: {}, cid {}", ex, cid);

        return new ResponseEntity<>(new HttpErrorResponse(cid, BAD_REQUEST_ERROR_ID,
                MISSING_PARAM), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpErrorResponse> defaultError(Exception ex) {
        var cid = MdcUtils.getCid();
        LOGGER.error("Error: {}, cid {}", ex, cid);

        return new ResponseEntity<>(new HttpErrorResponse(cid, DEFAULT_ERROR_ID, DEFAULT_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<HttpErrorResponse> userAlreadyExists(Exception ex) {
        var cid = MdcUtils.getCid();
        LOGGER.error("Error: {}, cid {}", ex, cid);

        return new ResponseEntity<>(new HttpErrorResponse(cid, USER_ALREADY_EXISTS_ERROR_ID, USER_ALREADY_EXISTS), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WrongMsisdnException.class)
    public ResponseEntity<HttpErrorResponse> wrongMsisdnFormat(Exception ex) {
        var cid = MdcUtils.getCid();
        LOGGER.error("Error: {}, cid {}", ex, cid);

        return new ResponseEntity<>(new HttpErrorResponse(cid, WRONG_MSISDN_FORMAT_ERROR_ID, WRONG_MSISDN_FORMAT), HttpStatus.BAD_REQUEST);
    }
}
