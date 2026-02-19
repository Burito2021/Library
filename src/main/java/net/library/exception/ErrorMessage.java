package net.library.exception;

public final class ErrorMessage {
    public static final String TOO_MAY_REQUESTS = "Too Many Requests";
    public static final String DEFAULT_ERROR = "global error";
    public static final String MISSING_PARAM = "mandatory param error";
    public static final String WRONG_MSISDN_FORMAT = "Wrong msisdn format";
    public static final String USER_ALREADY_EXISTS = "Username already exists in Db";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String TOKEN_BLACKLISTED = "Token is in the blacklist";
    public static final String INVALID_FINGERPRINT = "Fingerprint is invalid";
    public static final String USER_DISABLED = "User is disabled";
    public static final String TOKEN_EXPIRED = "Token expired";
    public static final String REFRESH_TOKEN_NOT_FOUND = "Refresh token not found";
    public static final String FILTER_LENGTH = "Filter length should be more that 2 letters";
    public static final String WRONG_STATE = "wrong state or role";
    public static final String USER_NOT_FOUND = "User is not found";

    private ErrorMessage() {
    }
}