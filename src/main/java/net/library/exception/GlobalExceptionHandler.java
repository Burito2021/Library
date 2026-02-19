package net.library.exception;

import lombok.extern.slf4j.Slf4j;
import net.library.model.mapper.HttpErrorResponse;
import net.library.util.MdcUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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

    @ExceptionHandler(UserDisabledException.class)
    @ResponseStatus
    public ResponseEntity<HttpErrorResponse> userDisabled(UserDisabledException ex) {
        return httpErrorResponseBuilder(ex, USER_DISABLED_ID, USER_DISABLED, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidFingerprint.class)
    @ResponseStatus
    public ResponseEntity<HttpErrorResponse> invalidFingerprint(InvalidFingerprint ex) {
        return httpErrorResponseBuilder(ex, INVALID_FINGERPRINT_ID, INVALID_FINGERPRINT, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenBlackListed.class)
    @ResponseStatus
    public ResponseEntity<HttpErrorResponse> tokenBlackListed(TokenBlackListed ex) {
        return httpErrorResponseBuilder(ex, BLACKLISTED_TOKEN_ID, TOKEN_BLACKLISTED, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidToken.class)
    @ResponseStatus
    public ResponseEntity<HttpErrorResponse> invalidRefreshToken(InvalidToken ex) {
        return httpErrorResponseBuilder(ex, INVALID_TOKEN_ID, INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenExpired.class)
    @ResponseStatus
    public ResponseEntity<HttpErrorResponse> tokenExpired(TokenExpired ex) {
        return httpErrorResponseBuilder(ex, EXPIRED_TOKEN_ID, TOKEN_EXPIRED, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RefreshTokenNotFound.class)
    @ResponseStatus
    public ResponseEntity<HttpErrorResponse> refreshTokenNotFound(RefreshTokenNotFound ex) {
        return httpErrorResponseBuilder(ex, REFRESH_TOKEN_NOTFOUND, REFRESH_TOKEN_NOT_FOUND, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus
    public ResponseEntity<HttpErrorResponse> badCredentials(BadCredentialsException ex) {
        return httpErrorResponseBuilder(ex, BAD_CREDENTIALS_ERROR_ID, INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
    }

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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<HttpErrorResponse> notFound() {
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