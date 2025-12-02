package net.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import net.library.config.security.SecurityConfig;
import net.library.model.entity.*;
import net.library.model.request.BookRequest;
import net.library.repository.*;
import net.library.repository.enums.BookAction;
import net.library.repository.enums.BookItemStatus;
import net.library.service.BookService;
import net.library.service.UserService;
import net.library.util.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;

import static net.library.tools.Tools.*;
import static net.library.util.HttpUtil.*;
import static net.library.util.Utils.getUUID;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class BookControllerTest {

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

    @AfterEach
    void cleanAfter() {
        bookService.removeAll();
        userService.deleteAll();
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookSuccess() throws Exception {
        final var title = "The Great Gatsby";
        final var author = "F. Scott Fitzgerald";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.id", not(hasLength(0))))
                .andExpect(jsonPath("$.title", is(title)))
                .andExpect(jsonPath("$.author", is(author)))
                .andExpect(jsonPath("$.description", is(description)))
                .andExpect(jsonPath("$.publisher", is(publisher)))
                .andExpect(jsonPath("$.edition", is(edition)))
                .andExpect(jsonPath("$.publication", is(publication)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookSuccessDbCheck() throws Exception {
        final var title = "The Great Gatsby";
        final var author = "F. Scott Fitzgerald";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated());

        final var books = bookRepository.findAll();
        final var book = books.getFirst();

        assertEquals(1, books.size());
        assertEquals(title, book.getTitle());
        assertEquals(title, book.getTitle());
        assertEquals(author, book.getAuthor());
        assertEquals(description, book.getDescription());
        assertEquals(publisher, book.getPublisher());
        assertEquals(edition, book.getEdition());
        assertEquals(publication, book.getPublicationYear());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookTitleEmptyString() throws Exception {
        final var title = "";
        final var author = "F. Scott Fitzgerald";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookTitleWithinMinLength() throws Exception {
        final var title = "1";
        final var author = "F. Scott Fitzgerald";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookTitleWithinMaxLength() throws Exception {
        final var title = "Thisisastringthatcontainsexactlyonehundredcharacterswithoutspacesandisnowexactlyonehundredlong123456";
        final var author = "F. Scott Fitzgerald";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookTitleExceedsMaxLengthError() throws Exception {
        final var title = "Thisisastringthatcontainsexactlyonehundredcharacterswithoutspacesandisnowexactlyonehundredlong1234561";
        final var author = "F. Scott Fitzgerald";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
        ;
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookTitleNullValue() throws Exception {
        final var author = "F. Scott Fitzgerald";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(null)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookAuthorEmptyString() throws Exception {
        final var title = "The Great Gatsby";
        final var author = "";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookAuthWithinMinLength() throws Exception {
        final var title = "1";
        final var author = "Thisisastringthatcontainsexactlyonehundredcharacte";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookAuthorWithinMaxLength() throws Exception {
        final var title = "1";
        final var author = "Thisisastringthatcontainsexactlyonehundredcharacte";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookAuthorExceedsMaxLengthError() throws Exception {
        final var title = "1";
        final var author = "Thisisastringthatcontainsexactlyonehundredcharacte1";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookDescriptionWithinMaxLength() throws Exception {
        final var title = "1";
        final var author = "Thisisastringthatcontainsexactlyonehundredcharacte";
        final var description = randomString(500);
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookDescriptionExceedsMaxLengthError() throws Exception {
        final var title = "1";
        final var author = "Thisisastringthatcontainsexactlyonehundredcharacte";
        final var description = randomString(501);
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookAuthorNullValue() throws Exception {
        final var title = "The Great Gatsby";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(null)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookPublisherEmptyString() throws Exception {
        final var title = "The Great Gatsby";
        final var author = "F. Scott Fitzgerald";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody))
                .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookPublisherWithinMinLength() throws Exception {
        final var title = "1";
        final var author = "Thisisastringthatcontainsexactlyonehundredcharacte";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = randomString(1);
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookPublisherWithinMaxLength() throws Exception {
        final var title = "1";
        final var author = "Thisisastringthatcontainsexactlyonehundredcharacte";
        final var description = randomString(45);
        final var publisher = randomString(100);
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody))
                .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookPublisherExceedsMaxLengthError() throws Exception {
        final var title = "1";
        final var author = "Thisisastringthatcontainsexactlyonehundredcharacte";
        final var description = randomString(23);
        final var publisher = randomString(101);
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookPublisherNullValue() throws Exception {
        final var title = "The Great Gatsby";
        final var author = "F. Scott Fitzgerald";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var edition = "3rd Edition";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(null)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookEditionWithinMaxLength() throws Exception {
        final var title = "1";
        final var author = "Thisisastringthatcontainsexactlyonehundredcharacte";
        final var description = randomString(45);
        final var publisher = randomString(12);
        final var edition = randomString(30);
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookEditionExceedsMaxLengthError() throws Exception {
        final var title = "1";
        final var author = "Thisisastringthatcontainsexactlyonehundredcharacte";
        final var description = randomString(23);
        final var publisher = randomString(33);
        final var edition = randomString(31);
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .description(description)
                .publisher(publisher)
                .edition(edition)
                .publication(publication)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookAbsenceOfNonMandatoryParams() throws Exception {
        final var title = "The Catcher in the Rye";
        final var author = "J.D. Salinger";
        final var publisher = "Little, Brown and Company";
        final var xCorrelationId = getUUID();

        final var requestBody = BookRequest.builder()
                .title(title)
                .author(author)
                .publisher(publisher)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + BOOKS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookItemsSuccess() throws Exception {
        final var title = "The Great Gatsby";
        final var author = "F. Scott Fitzgerald";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var edition = "3rd Edition";
        final var publisher = "Scribner";
        final var publication = 1925;
        final var xCorrelationId = getUUID();

        final var book = new Book()
                .setTitle(title)
                .setAuthor(author)
                .setDescription(description)
                .setPublisher(publisher)
                .setEdition(edition)
                .setPublicationYear(publication);

        bookRepository.save(book);

        final var bookId = bookRepository.findAll().getFirst().getId();

        final var requestBody = new BookItem()
                .setBookId(bookId);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + ITEMS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.bookItemId", not(hasLength(0))))
                .andExpect(jsonPath("$.user", is(nullValue())))
                .andExpect(jsonPath("$.borrowedAt", is(nullValue())))
                .andExpect(jsonPath("$.returnedAt", is(nullValue())));

        final var bookItem = bookItemRepository.findAll().getFirst();

        assertNotNull(bookItem.getId());
        assertEquals(bookId, bookItem.getBookId());
        assertEquals(BookItemStatus.AVAILABLE, bookItem.getStatus());
        assertNull(bookItem.getUserId());
        assertNull(bookItem.getBorrowedAt());
        assertNull(bookItem.getReturnedAt());
        assertNotNull(bookItem.getUpdatedAt());
        assertNull(bookItem.getDeletedAt());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookItemNameNullError() throws Exception {

        final var xCorrelationId = getUUID();

        final var requestBody = new BookItem()
                .setBookId(null);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + ITEMS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookItemsBookIdNotExist() throws Exception {
        final var xCorrelationId = getUUID();
        final var requestBody = new BookItem()
                .setBookId(UUID.randomUUID());

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + ITEMS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(104)))
                .andExpect(jsonPath(ERROR_MSG, is("Username already exists in Db")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void addBookItemsNullBookId() throws Exception {
        final var xCorrelationId = getUUID();
        final var requestBody = new BookItem()
                .setBookId(null);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + ITEMS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemBorrowInProgressSuccessfulUpdate() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
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
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isAccepted());

        final var bookItem = bookItemRepository.findAll().getFirst();

        assertEquals(bookItem.getStatus(), BookItemStatus.IN_PROGRESS);
        assertNotNull(bookItem.getBorrowedAt());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemBorrowRemoveSuccessfulUpdate() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
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

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=removed")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isAccepted());

        final var bookItem = bookItemRepository.findAll().getFirst();

        assertEquals(BookItemStatus.REMOVED, bookItem.getStatus());
        assertNotNull(bookItem.getBorrowedAt());
        assertNull(bookItem.getReturnedAt());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemBorrowUpdateWrongBookItemId() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var bookItemId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=in_progress")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isNotFound());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemBorrowUpdateWrongUserId() throws Exception {
        final var xCorrelationId = getUUID();

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = getUUID();
        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().getFirst().getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=in_progress")
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                )
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(104)))
                .andExpect(jsonPath(ERROR_MSG, is("Username already exists in Db")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemBorrowUpdateWrongStatus() throws Exception {
        final var xCorrelationId = getUUID();

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
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

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=removal")
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(110)))
                .andExpect(jsonPath(ERROR_MSG, is("wrong type format")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemBorrowBookIdNullUpdate() throws Exception {
        final var xCorrelationId = getUUID();

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
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

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + null + "/borrowing?" + "userId=" + userId + "&status=in_progress")
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(110)))
                .andExpect(jsonPath(ERROR_MSG, is("wrong type format")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemBorrowUserIdNullUpdate() throws Exception {
        final var xCorrelationId = getUUID();

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().getFirst().getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + null + "&status=in_progress")
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(110)))
                .andExpect(jsonPath(ERROR_MSG, is("wrong type format")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemReturn() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var user = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElseThrow();
        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setUserId(user)
                .setStatus(BookItemStatus.IN_PROGRESS));

        final var bookItemId = bookItemRepository.findAll().getFirst().getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                )
                .andExpect(status().isAccepted());

        final var bookItemAfterUpdate = bookItemRepository.findAll().getFirst();

        assertEquals(BookItemStatus.AVAILABLE, bookItemAfterUpdate.getStatus());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateActionBorrowAnyBookItemDbHistoryCheck() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
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

        final var response = mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookId + "/borrowingAny?" + "userId=" + userId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                )
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.bookItemId", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final var bookItemId = JsonPath.read(response, "$.bookItemId");

        final var bookHistory = bookItemHistoryRepository.findAll();

        assertEquals(2, bookHistory.size());
        assertNotNull(bookHistory.getFirst().getId());
        assertEquals(bookItemId, bookHistory.getFirst().getItemId().toString());
        assertNull(bookHistory.getFirst().getUserId());
        assertNotNull(bookHistory.getFirst().getActionAt());
        assertEquals(BookAction.ADDED, bookHistory.getFirst().getActionType());
        assertNotNull(bookHistory.get(1).getId());
        assertEquals(bookItemId, bookHistory.get(1).getItemId().toString());
        assertEquals(userId, bookHistory.get(1).getUserId());
        assertNotNull(bookHistory.getFirst().getActionAt());
        assertEquals(BookAction.BORROWED, bookHistory.get(1).getActionType());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemReturnDbHistoryCheck() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var user = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElseThrow();
        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setUserId(user)
                .setStatus(BookItemStatus.IN_PROGRESS));

        final var bookItemId = bookItemRepository.findAll().getFirst().getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                )
                .andExpect(status().isAccepted());

        final var bookHistory = bookItemHistoryRepository.findAll();

        assertEquals(2, bookHistory.size());
        assertNotNull(bookHistory.getFirst().getId());
        assertEquals(bookItemId, bookHistory.getFirst().getItemId());
        assertNull(bookHistory.getFirst().getUserId());
        assertNotNull(bookHistory.getFirst().getActionAt());
        assertEquals(BookAction.ADDED, bookHistory.getFirst().getActionType());
        assertNotNull(bookHistory.get(1).getId());
        assertEquals(bookItemId, bookHistory.get(1).getItemId());
        assertEquals(userId, bookHistory.get(1).getUserId());
        assertNotNull(bookHistory.getFirst().getActionAt());
        assertEquals(BookAction.RETURNED, bookHistory.get(1).getActionType());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemReturnWrongUserId() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = getUUID();
        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.IN_PROGRESS));

        final var bookItemId = bookItemRepository.findAll().getFirst().getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                )
                .andExpect(status().isNotFound());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemReturnWrongBookId() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var user = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElseThrow();
        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setUserId(user)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isNotFound());

    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getAllAvailableBooksSeveralItems() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().getFirst().getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=available")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(2)));
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getBookItemParamCheck() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().getFirst().getId(), userId, BookItemStatus.AVAILABLE);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=available")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].bookItemId", notNullValue()))
                .andExpect(jsonPath("$.items[0].bookItemId", not(hasLength(0))))
                .andExpect(jsonPath("$.items[0].user", is(nullValue())))
                .andExpect(jsonPath("$.items[0].borrowedAt", notNullValue()))
                .andExpect(jsonPath("$.items[0].returnedAt", is(nullValue())));
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getAllAvailableBooks() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().getFirst().getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=available")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(2)))
                .andExpect(jsonPath("$.items.length()", is(2)))
                .andExpect(jsonPath("$.items.length()", is(2)))
                .andExpect(jsonPath("$.items.length()", is(2)));
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getAllRemovedBooks() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().getFirst().getId(), userId, BookItemStatus.REMOVED);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=removed")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(1)));
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getAllInProgressBooks() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.IN_PROGRESS));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().getFirst().getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=in_progress")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(1)));
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getAllAvailableByBookItemId() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.IN_PROGRESS));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.IN_PROGRESS));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.REMOVED));

        final var bookItemId = bookItemRepository.findAll().getFirst().getId();

        bookService.borrowActionBookItemById(bookItemRepository.findAll().getFirst().getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?bookItemId=" + bookItemId + "&status=in_progress")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(1)))
                .andExpect(jsonPath("$.items[0].bookItemId", is(bookItemId.toString())));
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getAllAvailableSortByBookItemStatus() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.IN_PROGRESS));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.IN_PROGRESS));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.REMOVED));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().getFirst().getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?sortBy=status")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(4)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getAllAvailableNoItemsNoFilters() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(0)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getAllBooksReturnedItems() throws Exception {
        final var title = "The Great Gatsby";
        final var author = "F. Scott Fitzgerald";
        final var description = "A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.";
        final var publisher = "Scribner";
        final var edition = "3rd Edition";
        final var publicationYear = 1925;

        bookRepository.save(new Book()
                .setTitle(title)
                .setAuthor(author)
                .setDescription(description)
                .setPublisher(publisher)
                .setEdition(edition)
                .setPublicationYear(publicationYear));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].bookId", not(hasLength(0))))
                .andExpect(jsonPath("$.items[0].title", is(title)))
                .andExpect(jsonPath("$.items[0].author", is(author)))
                .andExpect(jsonPath("$.items[0].description", is(description)))
                .andExpect(jsonPath("$.items[0].edition", is(edition)))
                .andExpect(jsonPath("$.items[0].publicationYear", is(publicationYear)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getAllBooksSortByTitleDefaultOrderDesc() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        bookRepository.save(new Book()
                .setTitle("Pride and Prejudice")
                .setAuthor("Jane Austen")
                .setDescription("A classic romance novel")
                .setPublisher("T. Egerton")
                .setEdition("1st")
                .setPublicationYear(1813));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all" + "?sortBy=title")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[1].title", is("Pride and Prejudice")))
                .andExpect(jsonPath("$.items[0].title", is("The Great Gatsby")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getAllBooksSortByTitleCustomOrderAsc() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        bookRepository.save(new Book()
                .setTitle("Pride and Prejudice")
                .setAuthor("Jane Austen")
                .setDescription("A classic romance novel")
                .setPublisher("T. Egerton")
                .setEdition("1st")
                .setPublicationYear(1813));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all" + "?sortBy=title&order=asc")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title", is("Pride and Prejudice")))
                .andExpect(jsonPath("$.items[1].title", is("The Great Gatsby")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getAllBooksCustomOrderAsc() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        bookRepository.save(new Book()
                .setTitle("Pride and Prejudice")
                .setAuthor("Jane Austen")
                .setDescription("A classic romance novel")
                .setPublisher("T. Egerton")
                .setEdition("1st")
                .setPublicationYear(1813));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all" + "?order=asc")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[1].title", is("Pride and Prejudice")))
                .andExpect(jsonPath("$.items[0].title", is("The Great Gatsby")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void borrowAnyBookItemSuccess() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
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
        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var response = mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookId + "/borrowingAny?" + "userId=" + userId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.bookItemId", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final var bookItemId = JsonPath.read(response, "$.bookItemId");
        final var bookItem = bookItemRepository.findById(UUID.fromString(bookItemId.toString())).get();

        assertEquals(BookItemStatus.IN_PROGRESS, bookItem.getStatus());
        assertNotNull(bookItem.getBorrowedAt());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void borrowAnyBookItemNotAvailable() throws Exception {

        final var user = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElseThrow();

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
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
                .setUserId(user)
                .setStatus(BookItemStatus.AVAILABLE));

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setUserId(user)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItems = bookItemRepository.findAll();

        bookService.borrowActionBookItemById(bookItems.getFirst().getId(), userId, BookItemStatus.IN_PROGRESS);
        bookService.borrowActionBookItemById(bookItems.get(1).getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookId + "/borrowingAny?" + "userId=" + userId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void borrowAnyBookItemInvalidUser() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = Utils.getUUID();
        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookId + "/borrowingAny?" + "userId=" + userId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void borrowAnyBookItemInvalidBookId() throws Exception {

        final var userId = userRepository.findAll().getFirst().getId();
        final var bookId = Utils.getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookId + "/borrowingAny?" + "userId=" + userId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isNotFound());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getCountOfAvailableBookItems() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?bookId=" + bookId + "&status=available")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(3)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getCountOfAvailableBookItemsNoAvailableItems() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var bookId = bookRepository.findAll().getFirst().getId();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?bookId=" + bookId + "&status=available")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getCountOfAvailableBookItemsInvalidBookId() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var bookId = Utils.getUUID();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?bookId=" + bookId + "&status=available")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getAllBooksAndBookItems() throws Exception {

        final var user = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElseThrow();

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby2")
                .setAuthor("F. Scott Fitzgerald2")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.2")
                .setPublisher("Scribner2")
                .setEdition("3rd Edition2")
                .setPublicationYear(1930));

        final var bookId = bookRepository.findAll().getFirst().getId();

        final var genre = genreRepository.save(new Genre().setName("Action"));

        bookGenresRepository.save(
                new BookGenre()
                        .setBook_id(bookId)
                        .setGenre(genre));

        final var bookItem = bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setUserId(user)
                .setStatus(BookItemStatus.AVAILABLE));
        bookItem.setStatus(BookItemStatus.IN_PROGRESS);
        bookItem.setBorrowedAt(LocalDateTime.now());
        bookItemRepository.save(bookItem);

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all?order=asc")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.items[0].bookId", notNullValue()))
                .andExpect(jsonPath("$.items[0].title", is("The Great Gatsby")))
                .andExpect(jsonPath("$.items[0].author", is("F. Scott Fitzgerald")))
                .andExpect(jsonPath("$.items[0].description", is("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")))
                .andExpect(jsonPath("$.items[0].edition", is("3rd Edition")))
                .andExpect(jsonPath("$.items[0].publicationYear", is(1925)))
                .andExpect(jsonPath("$.items[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$.items[0].deletedAt", nullValue()))
                .andExpect(jsonPath("$.items[0].bookItems[0].bookItemId", notNullValue()))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.id", notNullValue()))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.username", is("user_1")))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.name", is("Name_940")))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.surname", is("Surname_934")))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.email", is("user_1@example.com")))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.phoneNumber", is("555946646")))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.address", is("Street 688, City 90, State 49")))
                .andExpect(jsonPath("$.items[0].bookItems[0].status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.items[0].bookItems[0].borrowedAt", notNullValue()))
                .andExpect(jsonPath("$.items[0].bookItems[0].returnedAt", nullValue()))
                .andExpect(jsonPath("$.items[0].bookItems[1].user", nullValue()))
                .andExpect(jsonPath("$.items[0].bookItems[1].status", is("AVAILABLE")))
                .andExpect(jsonPath("$.items[0].bookItems[1].borrowedAt", nullValue()))
                .andExpect(jsonPath("$.items[0].bookItems[1].returnedAt", nullValue()))
                .andExpect(jsonPath("$.items[0].bookGenres[0].genreName", is("Action")))
                .andExpect(jsonPath("$.items[1].bookId", notNullValue()))
                .andExpect(jsonPath("$.items[1].title", is("The Great Gatsby2")))
                .andExpect(jsonPath("$.items[1].author", is("F. Scott Fitzgerald2")))
                .andExpect(jsonPath("$.items[1].description", is("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.2")))
                .andExpect(jsonPath("$.items[1].edition", is("3rd Edition2")))
                .andExpect(jsonPath("$.items[1].publicationYear", is(1930)))
                .andExpect(jsonPath("$.items[1].createdAt", notNullValue()))
                .andExpect(jsonPath("$.items[1].deletedAt", nullValue()))
                .andExpect(jsonPath("$.items[1].bookItems", is(hasSize(0))))
                .andExpect(jsonPath("$.items[1].bookGenres", is(hasSize(0))))
                .andDo(print());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getAllBooksAnd100BookItems() throws Exception {
        final var user = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElseThrow();

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var bookId = bookRepository.findAll().getFirst().getId();

        final var genre = genreRepository.save(new Genre().setName("Action"));

        bookGenresRepository.save(
                new BookGenre()
                        .setBook_id(bookId)
                        .setGenre(genre));

        populateWithBookItems(
                bookItemRepository,
                bookId,
                user,
                BookItemStatus.IN_PROGRESS,
                LocalDateTime.now(),
                100);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all?order=asc")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items[0].bookItems", hasSize(100)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getAllBooksAndBookItemsEmptyResponse() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getBookById() throws Exception {
        final var user = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElseThrow();

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var bookId = bookRepository.findAll().getFirst().getId();

        final var genre = genreRepository.save(new Genre().setName("Action"));

        bookGenresRepository.save(
                new BookGenre()
                        .setBook_id(bookId)
                        .setGenre(genre));

        final var bookItem = bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setUserId(user)
                .setStatus(BookItemStatus.AVAILABLE));
        bookItem.setStatus(BookItemStatus.IN_PROGRESS);
        bookItem.setBorrowedAt(LocalDateTime.now());
        bookItemRepository.save(bookItem);

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/" + bookId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId", notNullValue()))
                .andExpect(jsonPath("$.title", is("The Great Gatsby")))
                .andExpect(jsonPath("$.author", is("F. Scott Fitzgerald")))
                .andExpect(jsonPath("$.description", is("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")))
                .andExpect(jsonPath("$.edition", is("3rd Edition")))
                .andExpect(jsonPath("$.publicationYear", is(1925)))
                .andExpect(jsonPath("$.publicationYear", notNullValue()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.deletedAt", nullValue()))
                .andExpect(jsonPath("$.bookItems[0].bookItemId", notNullValue()))
                .andExpect(jsonPath("$.bookItems[0].user.id", notNullValue()))
                .andExpect(jsonPath("$.bookItems[0].user.username", is("user_1")))
                .andExpect(jsonPath("$.bookItems[0].user.name", is("Name_940")))
                .andExpect(jsonPath("$.bookItems[0].user.surname", is("Surname_934")))
                .andExpect(jsonPath("$.bookItems[0].user.email", is("user_1@example.com")))
                .andExpect(jsonPath("$.bookItems[0].user.phoneNumber", is("555946646")))
                .andExpect(jsonPath("$.bookItems[0].user.address", is("Street 688, City 90, State 49")))
                .andExpect(jsonPath("$.bookItems[0].status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.bookItems[0].borrowedAt", notNullValue()))
                .andExpect(jsonPath("$.bookItems[0].returnedAt", nullValue()))
                .andExpect(jsonPath("$.bookItems[1].user", nullValue()))
                .andExpect(jsonPath("$.bookItems[1].status", is("AVAILABLE")))
                .andExpect(jsonPath("$.bookItems[1].borrowedAt", nullValue()))
                .andExpect(jsonPath("$.bookItems[1].returnedAt", nullValue()))
                .andExpect(jsonPath("$.bookGenres[0].genreName", is("Action")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getBookByIdNotFound() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/" + UUID.randomUUID())
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void borrowAnyBookItem2ThreadsSuccess() {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var bookId = bookRepository.findAll().getFirst().getId();

        final var bookItemIdOne = bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE)).getId();
        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE)).getId();
        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE)).getId();

        bookItemRepository.findAll().forEach(bookItem -> {
            bookItem.setStatus(BookItemStatus.IN_PROGRESS);
            bookItem.setBorrowedAt(Utils.currentDate());
            bookItem.setUserId(new User().setId(userId));
            bookItemRepository.save(bookItem);
        });

        var listOfTreads = getCallables(GLOBAL_BASE_URI + ITEMS + "/" + bookItemIdOne + "/returnTransaction?userId=" + userId, 2);

        var statusCodes = threadRunner(2, listOfTreads);

        Collections.sort(statusCodes);

        assertEquals(2,statusCodes.size());
        assertEquals(202,statusCodes.getFirst());
        assertEquals(500,statusCodes.get(1));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateBookItemBorrow2ThreadsSuccess() throws Exception {

        bookRepository.save(new Book().setTitle("The Great Gatsby").setAuthor("F. Scott Fitzgerald").setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.").setPublisher("Scribner").setEdition("3rd Edition").setPublicationYear(1925));

        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem().setBookId(bookId).setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().getFirst().getId();

        var listOfTreads = getCallables(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=in_progress", 3);

        var statusCodes = threadRunner(2, listOfTreads);

        Collections.sort(statusCodes);

        assertEquals(3, statusCodes.size());
        assertEquals(202, statusCodes.getFirst());
        assertEquals(404, statusCodes.get(1));
    }

    private List<Callable<Integer>> getCallables(String url, int count) {
        List<Callable<Integer>> listOfThreads = new ArrayList<>();
        for (int x = 0; x < count; x++) {
            listOfThreads.add(() -> {
                var response = mvc.perform(MockMvcRequestBuilders.patch(url)
                        .with(httpBasic("user_1", PASSWORD_ADMIN))).andReturn().getResponse();

                return response.getStatus();
            });
        }

        return listOfThreads;
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void userWithUerRoleTypeHasPermissionToRetrieveBooks() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        bookItemRepository.save(new BookItem()
                .setBookId(book.getFirst().getId())
                .setStatus(BookItemStatus.AVAILABLE));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().getFirst().getId(), userId, BookItemStatus.AVAILABLE);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=available")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].bookItemId", notNullValue()))
                .andExpect(jsonPath("$.items[0].bookItemId", not(hasLength(0))))
                .andExpect(jsonPath("$.items[0].user", is(nullValue())))
                .andExpect(jsonPath("$.items[0].borrowedAt", notNullValue()))
                .andExpect(jsonPath("$.items[0].returnedAt", is(nullValue())));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void roleTypeIsUserUpdateBookItemForbidden() throws Exception {

        bookRepository.save(new Book().setTitle("The Great Gatsby").setAuthor("F. Scott Fitzgerald").setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.").setPublisher("Scribner").setEdition("3rd Edition").setPublicationYear(1925));

        final var userId = userRepository.findAll().stream().sorted(Comparator.comparing(User::getUsername)).findFirst().map(User::getId).orElseThrow();
        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem().setBookId(bookId).setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().getFirst().getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=in_progress").with(httpBasic("user_2", PASSWORD_USER))).andExpect(status().isForbidden());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void roleTypeIsUserDeleteBookItemForbidden() throws Exception {

        bookRepository.save(new Book().setTitle("The Great Gatsby").setAuthor("F. Scott Fitzgerald").setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.").setPublisher("Scribner").setEdition("3rd Edition").setPublicationYear(1925));

        final var bookId = bookRepository.findAll().getFirst().getId();

        bookItemRepository.save(new BookItem().setBookId(bookId).setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().getFirst().getId();
        bookItemHistoryRepository.deleteAll();
        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId).with(httpBasic("user_2", PASSWORD_USER))).andExpect(status().isForbidden());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void roleTypeIsUserDeleteBookForbidden() throws Exception {

        bookRepository.save(new Book().setTitle("The Great Gatsby").setAuthor("F. Scott Fitzgerald").setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.").setPublisher("Scribner").setEdition("3rd Edition").setPublicationYear(1925));

        final var bookId = bookRepository.findAll().getFirst().getId();

        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI + ITEMS + "/" + bookId).with(httpBasic("user_2", PASSWORD_USER))).andDo(print()).andExpect(status().isForbidden());
    }
}