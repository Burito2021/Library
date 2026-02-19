package net.library.exception;

public final class ErrorId {
    public static final int TOO_MANY_REQUESTS_ID = 429;
    public static final int DEFAULT_ERROR_ID = 101;
    public static final int BAD_REQUEST_ERROR_ID = 102;
    public static final int WRONG_MSISDN_FORMAT_ERROR_ID = 103;
    public static final int USER_ALREADY_EXISTS_ERROR_ID = 104;
    public static final int BAD_CREDENTIALS_ERROR_ID = 108;
    public static final int INVALID_TOKEN_ID = 109;
    public static final int BLACKLISTED_TOKEN_ID = 111;
    public static final int INVALID_FINGERPRINT_ID = 113;
    public static final int USER_DISABLED_ID = 114;
    public static final int EXPIRED_TOKEN_ID = 110;
    public static final int REFRESH_TOKEN_NOTFOUND = 112;
    public static final int FILTER_ID = 105;
    public static final int WRONG_STATE_ID = 106;
    public static final int USER_NOT_FOUND_ID = 107;
    private ErrorId() {
    }
}

