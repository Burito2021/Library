package net.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.library.TestContainers;
import net.library.model.entity.Book;
import net.library.model.entity.BookItem;
import net.library.model.entity.User;
import net.library.model.request.LoginRequest;
import net.library.model.request.RefreshTokenRequest;
import net.library.repository.*;
import net.library.repository.enums.BookItemStatus;
import net.library.service.AuthService;
import net.library.service.BookService;
import net.library.service.RateLimitService;
import net.library.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static net.library.exception.ErrorMessage.MISSING_PARAM;
import static net.library.tools.HttpUtil.*;
import static net.library.tools.Tools.*;
import static net.library.util.HttpUtil.*;
import static net.library.util.Utils.getUUID;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthControllerTest extends TestContainers {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private BookService bookService;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookItemRepository bookItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookGenresRepository bookGenresRepository;
    @Autowired
    private BookItemHistoryRepository bookItemHistoryRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;
    @Autowired
    private RateLimitService rateLimitService;

    @AfterEach
    void clean() {
        bookItemHistoryRepository.deleteAll();
        genreRepository.deleteAll();
        bookGenresRepository.deleteAll();
        bookItemRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void authLoginReturnsUnauthorizedWhenUserIsNotInDb() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = LoginRequest.builder()
                .username("Alex")
                .password("gogl")
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGIN)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))

                .andExpect(status().isUnauthorized());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLoginReturnsUnauthorizedWhenInvalidUsername() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = LoginRequest.builder()
                .username("Alex")
                .password(PASSWORD_ADMIN)
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGIN)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(108)))
                .andExpect(jsonPath("$.errorMsg", is("Invalid credentials")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLoginReturnsUnauthorizedWhenInvalidPassword() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = LoginRequest.builder()
                .username(ADMIN_USER)
                .password("sdfsdfdsf")
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGIN)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(108)))
                .andExpect(jsonPath("$.errorMsg", is("Invalid credentials")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLoginReturnsUnauthorizedWhenUsernameIsEmptyString() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = LoginRequest.builder()
                .username("")
                .password(PASSWORD_ADMIN)
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGIN)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLoginReturnsUnauthorizedWhenUsernameIsNull() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = LoginRequest.builder()
                .username(null)
                .password(PASSWORD_ADMIN)
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGIN)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLoginReturnsUnauthorizedWhenPasswordIsEmptyString() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = LoginRequest.builder()
                .username(ADMIN_USER)
                .password("")
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGIN)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLoginReturnsUnauthorizedWhenPasswordIsNull() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = LoginRequest.builder()
                .username(ADMIN_USER)
                .password(null)
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGIN)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLoginReturnsUnauthorizedWhenFingerprintIsEmptyString() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = LoginRequest.builder()
                .username(ADMIN_USER)
                .password(PASSWORD_ADMIN)
                .fingerprint("")
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGIN)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLoginReturnsUnauthorizedWhenFingerprintIsNull() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = LoginRequest.builder()
                .username(ADMIN_USER)
                .password(PASSWORD_ADMIN)
                .fingerprint(null)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGIN)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void appReturnsUnauthorizedWhenInvalidAccessTokenIsUsed() throws Exception {
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + getUUID() + "/" + "state")
                        .queryParam("state", "")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .header(AUTHORIZATION, BEARER + "sdfsdfsdfdstryre"))
                .andExpect(status().isUnauthorized());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRefreshReturnsNewAccessTokenWhenValidRefreshToken() throws Exception {
        final var xCorrelationId = getUUID();
        final var tokens = getTokens(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);
        final var accessToken = tokens.getAccessToken();
        final var refreshToken = tokens.getRefreshToken();

        Thread.sleep(2000);

        final var requestBody = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REFRESH)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(accessToken)))
                .andExpect(jsonPath("$.refreshToken", not(refreshToken)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRefreshReturnsUnauthorizedWhenInValidRefreshToken() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = RefreshTokenRequest.builder()
                .refreshToken(INVALID_REFRESH_TOKEN)
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REFRESH)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(109)))
                .andExpect(jsonPath("$.errorMsg", is("Invalid token")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRefreshReturnsUnauthorizedWhenExpiredToken() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = RefreshTokenRequest.builder()
                .refreshToken(EXPIRED_REFRESH_TOKEN)
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REFRESH)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("Token expired")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRefreshReturnsUnauthorizedWhenRefreshTokenIsEmptyString() throws Exception {
        final var xCorrelationId = getUUID();
        final var requestBody = RefreshTokenRequest.builder()
                .refreshToken("")
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REFRESH)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is(MISSING_PARAM)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRefreshReturnsUnauthorizedWhenRefreshTokenIsNull() throws Exception {
        final var xCorrelationId = getUUID();
        final var requestBody = RefreshTokenRequest.builder()
                .refreshToken("")
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REFRESH)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is(MISSING_PARAM)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRefreshReturnsUnauthorizedWhenFingerprintIsEmptyString() throws Exception {
        final var xCorrelationId = getUUID();
        final var tokens = getTokens(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);
        final var refreshToken = tokens.getRefreshToken();

        final var requestBody = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .fingerprint("")
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REFRESH)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is(MISSING_PARAM)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRefreshReturnsUnauthorizedWhenFingerprintIsNull() throws Exception {
        final var xCorrelationId = getUUID();
        final var tokens = getTokens(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);
        final var refreshToken = tokens.getRefreshToken();
        final var requestBody = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .fingerprint(null)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REFRESH)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is(MISSING_PARAM)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRevokeReturnsUnauthorizedWhenAccessTokenIsInValid() throws Exception {
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REVOKE)
                        .header(AUTHORIZATION, BEARER + INVALID_ACCESS_TOKEN)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorId", is(109)))
                .andExpect(jsonPath("$.errorMsg", is("Invalid token")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRevokeReturns200OkWhenAccessTokenIsNull() throws Exception {
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REVOKE)
                        .header(AUTHORIZATION, BEARER + null)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRevokeReturns200OkWhenAccessTokenIsEmptyString() throws Exception {
        final var xCorrelationId = getUUID();
        var accessToken = "";

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REVOKE)
                        .header(AUTHORIZATION, BEARER + accessToken)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRevokeReturns200OkWhenAccessTokenIsInvalid() throws Exception {
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REVOKE)
                        .header(AUTHORIZATION, BEARER + INVALID_ACCESS_TOKEN)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorId", is(109)))
                .andExpect(jsonPath("$.errorMsg", is("Invalid token")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authRevokeReturns200OkWhenAccessTokenIsExpired() throws Exception {
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REVOKE)
                        .header(AUTHORIZATION, BEARER + EXPIRED_ACCESS_TOKEN)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("Token expired")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLogoutReturns200OkWhenAccessTokenIsValid() throws Exception {
        final var xCorrelationId = getUUID();
        final var tokens = getTokens(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);
        final var accessToken = tokens.getAccessToken();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGOUT)
                        .header(AUTHORIZATION, BEARER + accessToken)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLogoutReturnsUnauthorizedWhenAccessTokenIsInvalid() throws Exception {
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGOUT)
                        .header(AUTHORIZATION, BEARER + INVALID_ACCESS_TOKEN)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorId", is(109)))
                .andExpect(jsonPath("$.errorMsg", is("Invalid token")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLogoutReturnsUnauthorizedWhenAccessTokenIsExpired() throws Exception {
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGOUT)
                        .header(AUTHORIZATION, BEARER + EXPIRED_ACCESS_TOKEN)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("Token expired")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLogoutReturnsUnauthorizedWhenAccessTokenIsEmptyString() throws Exception {
        final var xCorrelationId = getUUID();
        var accessToken = "";

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGOUT)
                        .header(AUTHORIZATION, BEARER + accessToken)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void authLogoutReturnsUnauthorizedWhenAccessTokenIsNull() throws Exception {
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGOUT)
                        .header(AUTHORIZATION, BEARER + null)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void refreshTokenReturns401WhenLogout() throws Exception {
        final var xCorrelationId = getUUID();
        final var tokens = getTokens(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);
        final var refreshToken = tokens.getRefreshToken();
        final var accessToken = tokens.getAccessToken();

        revokeAndLogout(authService, accessToken);

        final var requestBody = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REFRESH)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(112)))
                .andExpect(jsonPath("$.errorMsg", is("Refresh token not found")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void refreshTokenReturns401WhenRevoked() throws Exception {
        final var xCorrelationId = getUUID();
        final var tokens = getTokens(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);
        final var refreshToken = tokens.getRefreshToken();
        final var accessToken = tokens.getAccessToken();

        revokeAndLogout(authService, accessToken);

        final var requestBody = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REFRESH)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(112)))
                .andExpect(jsonPath("$.errorMsg", is("Refresh token not found")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void returns401WhenLogout() throws Exception {
        final var xCorrelationId = getUUID();
        final var tokens = getTokens(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);
        final var refreshToken = tokens.getRefreshToken();
        final var accessToken = tokens.getAccessToken();

        revokeAndLogout(authService, accessToken);

        final var requestBody = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .fingerprint(FINGERPRINT)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_REFRESH)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(112)))
                .andExpect(jsonPath("$.errorMsg", is("Refresh token not found")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemReturnsUnauthorizedWhenLoggedOutOrRevoked() throws Exception {
        final var tokens = getTokens(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);
        final var accessToken = tokens.getAccessToken();

        revokeAndLogout(authService, accessToken);

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby1")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().getFirst().getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=in_progress")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authLoginRateLimiterTest() throws Exception {
        rateLimitService.setEnabled(true);
        rateLimitService.setMaxAttempts(1);
        rateLimitService.setWindowSeconds(1);
        try {
            var numberOfRequests = 5;

            var requestBody = LoginRequest.builder()
                    .username("nonexistent_user")
                    .password(PASSWORD_TEST)
                    .fingerprint(FINGERPRINT)
                    .build();

            List<Integer> statusCodes = new ArrayList<>();

            for (int i = 0; i < numberOfRequests; i++) {
                var response = mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + AUTH_LOGIN)
                                .content(objectMapper.writeValueAsString(requestBody))
                                .header(CORRELATION_ID_HEADER_NAME, UUID.randomUUID().toString())
                                .contentType(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

                statusCodes.add(response.getStatus());
            }

            System.out.println("Status codes: " + statusCodes);

            assertEquals(401, statusCodes.get(0));
            assertEquals(429, statusCodes.get(1));
        } finally {
            rateLimitService.setEnabled(false);
            rateLimitService.setMaxAttempts(5);
            rateLimitService.setWindowSeconds(900);
        }
    }
}
