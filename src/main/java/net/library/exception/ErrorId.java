package net.library.exception;

public final class ErrorId {
    private ErrorId() {}

    protected static final Integer DEFAULT_ERROR_ID = 101;
    protected static final Integer BAD_REQUEST_ERROR_ID = 102;
    protected static final Integer WRONG_MSISDN_FORMAT_ERROR_ID = 103;
    protected static final Integer USER_ALREADY_EXISTS_ERROR_ID = 104;
    protected static final Integer BAD_CREDENTIALS_ERROR_ID = 108;
    protected static final Integer INVALID_TOKEN_ID = 109;
    protected static final Integer EXPIRED_TOKEN_ID = 110;
    protected static final Integer REFRESH_TOKEN_NOTFOUND = 111;
    protected static final Integer FILTER_ID = 105;
    protected static final Integer WRONG_STATE_ID = 106;
    protected static final Integer USER_NOT_FOUND_ID = 107;
}

