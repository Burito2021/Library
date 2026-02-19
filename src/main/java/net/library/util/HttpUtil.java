package net.library.util;

public class HttpUtil {
    public static final String GLOBAL_BASE_URI = "/api/v1/";
    public static final String BOOKS = "books";
    public static final String GENRES = "genres";
    public static final String ITEMS = "items";
    public static final String USERS = "users";
    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_REFRESH = "/auth/refresh";
    public static final String AUTH_REVOKE = "/auth/revoke";
    public static final String AUTH_LOGOUT = "/auth/logout";
    public static final String URL_ALL = "/**";
    public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
    public static final String PASSWORD_ADMIN = "ADMIN@123";
    public static final String ADMIN_USER = "user1";
    public static final String COMMON_USER = "user2";
    public static final String ADMIN_USER2 = "user2";
    public static final String INVALID_REFRESH_TOKEN = "1yJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTc3MTkzOTQ4NywiZXhwIjoxNzcxOTM5NjA3fQ.N1dfKoDsZZ6XrNOYGTY_pY1fVPE2_F-XLbcYnFQr_w4Oi4CvkYuhJkibD0JuTq5L3uGzv_QSuC4f_zds275RfA";
    public static final String EXPIRED_REFRESH_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTc3MTkzOTQ4NywiZXhwIjoxNzcxOTM5NjA3fQ.N1dfKoDsZZ6XrNOYGTY_pY1fVPE2_F-XLbcYnFQr_w4Oi4CvkYuhJkibD0JuTq5L3uGzv_QSuC4f_zds275RfA";
    public static final String INVALID_ACCESS_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInV1ZXJJZCI6IjZlZWUyMmUzLTlmZTAtNDI1My05NDc5LTAxZjBkNTJiYmY1OSIsInN1YiI6InVzZXIxIiwiaWF0IjoxNzcxOTQzMzI2LCJleHAiOjE3NzE5NDMzODZ9.2Yy-IUYvUJ1RM-wb-3XcS7Jq33YlpKyA0C08htBviZvuARkf5iWzw9izqLZ02JgiZtEGPSCGwIi7UnSjsKYHIA";
    public static final String EXPIRED_ACCESS_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInVzZXJJZCI6IjZlZWUyMmUzLTlmZTAtNDI1My05NDc5LTAxZjBkNTJiYmY1OSIsInN1YiI6InVzZXIxIiwiaWF0IjoxNzcxOTQzOTgzLCJleHAiOjE3NzE5NDQwNDN9.uwdOvAlicqpVjetjkLTNnLadP-JAePg8z9g_FBgvzp9K786TKuE_hJWLIrwZ92dxVCe4_qhr4_QgDNZUP8ntEg";
    public static final String COMMON_USER1 = "user1";
    public static final String PASSWORD_USER = "USER@123";
    public static final String PASSWORD_TEST = "Test#1234";
    public static final String ERROR_ID = "errorId";
    public static final String ERROR_MSG = "errorMsg";
    public static final String CID = "cid";
    public static final String MODERATION_STATE = "moderation_state";
    public static final String USER_STATE = "user_state";
    public static final String ROLE = "role";
    public static final String BOOK_GENRE = "/book_genre";
    public static final String BOOK_ITEM = "/book_item";
    public static final String GENRE_NAME = "genreName";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
}