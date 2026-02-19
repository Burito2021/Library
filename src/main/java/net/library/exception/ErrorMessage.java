package net.library.exception;

public final class ErrorMessage {
    private ErrorMessage() {}

    protected static final String DEFAULT_ERROR = "global error";
    protected static final String MISSING_PARAM = "mandatory param error";
    protected static final String WRONG_MSISDN_FORMAT = "Wrong msisdn format";
    protected static final String USER_ALREADY_EXISTS = "Username already exists in Db";
    protected static final String INVALID_CREDENTIALS = "Invalid credentials";
    protected static final String INVALID_TOKEN = "Invalid token";
    protected static final String TOKEN_EXPIRED = "Token expired";
    protected static final String REFRESH_TOKEN_NOT_FOUND = "Refresh token not found";
    protected static final String FILTER_LENGTH = "Filter length should be more that 2 letters";
    protected static final String WRONG_STATE = "wrong state or role";
    protected static final String USER_NOT_FOUND = "User is not found";
}