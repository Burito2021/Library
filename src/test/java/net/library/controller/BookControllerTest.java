package net.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import net.library.model.entity.*;
import net.library.model.request.BookRequest;
import net.library.repository.*;
import net.library.repository.enums.BookAction;
import net.library.repository.enums.BookItemStatus;
import net.library.service.BookService;
import net.library.util.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static net.library.tools.Tools.*;
import static net.library.util.HttpUtil.*;
import static net.library.util.Utils.getUUID;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
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

    @AfterEach
    void cleanAfter() {
        bookService.removeAll();
        userRepository.deleteAll();
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated());

        final var books = bookRepository.findAll();
        final var book = books.get(0);

        assertEquals(1, books.size());
        assertEquals(title, book.getTitle());
        assertEquals(title, book.getTitle());
        assertEquals(author, book.getAuthor());
        assertEquals(description, book.getDescription());
        assertEquals(publisher, book.getPublisher());
        assertEquals(edition, book.getEdition());
        assertEquals(publication, book.getPublicationYear());
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

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
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }


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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

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
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }


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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

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
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated());
    }

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

        final var bookId = bookRepository.findAll().get(0).getId();

        final var requestBody = new BookItem()
                .setBookId(bookId);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + ITEMS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.bookItemId", not(hasLength(0))))
                .andExpect(jsonPath("$.user", is(nullValue())))
                .andExpect(jsonPath("$.borrowedAt", is(nullValue())))
                .andExpect(jsonPath("$.returnedAt", is(nullValue())));

        final var bookItem = bookItemRepository.findAll().get(0);

        assertNotNull(bookItem.getId());
        assertEquals(bookId, bookItem.getBookId());
        assertEquals(BookItemStatus.AVAILABLE, bookItem.getStatus());
        assertNull(bookItem.getUserId());
        assertNull(bookItem.getBorrowedAt());
        assertNull(bookItem.getReturnedAt());
        assertNotNull(bookItem.getUpdatedAt());
        assertNull(bookItem.getDeletedAt());
    }

    @Test
    void addBookItemNameNullError() throws Exception {

        final var xCorrelationId = getUUID();

        final var requestBody = new BookItem()
                .setBookId(null);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + ITEMS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    @Test
    void addBookItemsBookIdNotExist() throws Exception {
        final var xCorrelationId = getUUID();
        final var requestBody = new BookItem()
                .setBookId(UUID.randomUUID());

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + ITEMS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(104)))
                .andExpect(jsonPath(ERROR_MSG, is("Username already exists in Db")));
    }

    @Test
    void addBookItemsNullBookId() throws Exception {
        final var xCorrelationId = getUUID();
        final var requestBody = new BookItem()
                .setBookId(null);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + ITEMS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    //this
    @Test
    void updateBookItemBorrowInProgressSuccessfulUpdate() throws Exception {

        userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=in_progress"))
                .andExpect(status().isAccepted());

        final var bookItem = bookItemRepository.findAll().get(0);

        assertEquals(bookItem.getStatus(), BookItemStatus.IN_PROGRESS);
        assertNotNull(bookItem.getBorrowedAt());
    }

    @Test
    void updateBookItemBorrowRemoveSuccessfulUpdate() throws Exception {

        userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=removed"))
                .andExpect(status().isAccepted());

        final var bookItem = bookItemRepository.findAll().get(0);

        assertEquals(BookItemStatus.REMOVED, bookItem.getStatus());
        assertNotNull(bookItem.getBorrowedAt());
        assertNull(bookItem.getReturnedAt());
    }

    @Test
    void updateBookItemBorrowUpdateWrongBookItemId() throws Exception {

        userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookItemId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=in_progress"))
                .andExpect(status().isNotFound());
    }

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
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=in_progress")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                )
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(104)))
                .andExpect(jsonPath(ERROR_MSG, is("Username already exists in Db")));
    }

    @Test
    void updateBookItemBorrowUpdateWrongStatus() throws Exception {
        final var xCorrelationId = getUUID();

        userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=removal")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(110)))
                .andExpect(jsonPath(ERROR_MSG, is("wrong type format")));
    }

    @Test
    void updateBookItemBorrowBookIdNullUpdate() throws Exception {
        final var xCorrelationId = getUUID();

        userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + null + "/borrowing?" + "userId=" + userId + "&status=in_progress")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(110)))
                .andExpect(jsonPath(ERROR_MSG, is("wrong type format")));
    }

    @Test
    void updateBookItemBorrowUserIdNullUpdate() throws Exception {
        final var xCorrelationId = getUUID();

        userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + null + "&status=in_progress")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(110)))
                .andExpect(jsonPath(ERROR_MSG, is("wrong type format")));
    }

    @Test
    void updateBookItemReturn() throws Exception {

        final var user = userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setUserId(user)
                .setStatus(BookItemStatus.IN_PROGRESS));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId))
                .andExpect(status().isAccepted());

        final var bookItemAfterUpdate = bookItemRepository.findAll().get(0);

        assertEquals(BookItemStatus.AVAILABLE, bookItemAfterUpdate.getStatus());
    }

    //add tests to check history for all the actions returned/ borrowed
    @Test
    void updateActionBorrowAnyBookItemDbHistoryCheck() throws Exception {

        userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var response = mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookId + "/borrowingAny?" + "userId=" + userId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.bookItemId", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final var bookItemId = JsonPath.read(response, "$.bookItemId");

        final var bookHistory = bookItemHistoryRepository.findAll();

        assertEquals(2, bookHistory.size());
        assertNotNull(bookHistory.get(0).getId());
        assertEquals(bookItemId, bookHistory.get(0).getItemId().toString());
        assertNull(bookHistory.get(0).getUserId());
        assertNotNull(bookHistory.get(0).getActionAt());
        assertEquals(BookAction.ADDED, bookHistory.get(0).getActionType());
        assertNotNull(bookHistory.get(1).getId());
        assertEquals(bookItemId, bookHistory.get(1).getItemId().toString());
        assertEquals(userId, bookHistory.get(1).getUserId());
        assertNotNull(bookHistory.get(0).getActionAt());
        assertEquals(BookAction.BORROWED, bookHistory.get(1).getActionType());
    }

    @Test
    void updateBookItemReturnDbHistoryCheck() throws Exception {

        final var user = userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setUserId(user)
                .setStatus(BookItemStatus.IN_PROGRESS));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId))
                .andExpect(status().isAccepted());

        final var bookHistory = bookItemHistoryRepository.findAll();

        assertEquals(2, bookHistory.size());
        assertNotNull(bookHistory.get(0).getId());
        assertEquals(bookItemId, bookHistory.get(0).getItemId());
        assertNull(bookHistory.get(0).getUserId());
        assertNotNull(bookHistory.get(0).getActionAt());
        assertEquals(BookAction.ADDED, bookHistory.get(0).getActionType());
        assertNotNull(bookHistory.get(1).getId());
        assertEquals(bookItemId, bookHistory.get(1).getItemId());
        assertEquals(userId, bookHistory.get(1).getUserId());
        assertNotNull(bookHistory.get(0).getActionAt());
        assertEquals(BookAction.RETURNED, bookHistory.get(1).getActionType());
    }


    @Test
    void updateBookItemReturnWrongUserId() throws Exception {

        userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = getUUID();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.IN_PROGRESS));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBookItemReturnWrongBookId() throws Exception {

        final var user = userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setUserId(user)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId))
                .andExpect(status().isNotFound());

    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getAllAvailableBooksSeveralItems() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(2)));
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getBookItemParamCheck() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.AVAILABLE);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=available"))
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
        final var userId = userRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=available"))
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
        final var userId = userRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.REMOVED);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=removed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(1)));
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getAllInProgressBooks() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.IN_PROGRESS));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=in_progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(1)));
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getAllAvailableByBookItemId() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.IN_PROGRESS));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.IN_PROGRESS));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.REMOVED));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        bookService.borrowActionBookItemById(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?bookItemId=" + bookItemId + "&status=in_progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(1)))
                .andExpect(jsonPath("$.items[0].bookItemId", is(bookItemId.toString())));
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getAllAvailableSortByBookItemStatus() throws Exception {
        final var book = bookRepository.findAll();
        final var userId = userRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.IN_PROGRESS));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.IN_PROGRESS));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(book.get(0).getId())
                .setStatus(BookItemStatus.REMOVED));

        bookService.borrowActionBookItemById(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?sortBy=status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(4)));
    }

    @Test
    void getAllAvailableNoItemsNoFilters() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(0)));
    }


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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].bookId", not(hasLength(0))))
                .andExpect(jsonPath("$.items[0].title", is(title)))
                .andExpect(jsonPath("$.items[0].author", is(author)))
                .andExpect(jsonPath("$.items[0].description", is(description)))
                .andExpect(jsonPath("$.items[0].edition", is(edition)))
                .andExpect(jsonPath("$.items[0].publicationyYear", is(publicationYear)));
    }

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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all" + "?sortBy=title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[1].title", is("Pride and Prejudice")))
                .andExpect(jsonPath("$.items[0].title", is("The Great Gatsby")));
    }

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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all" + "?sortBy=title&order=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title", is("Pride and Prejudice")))
                .andExpect(jsonPath("$.items[1].title", is("The Great Gatsby")));
    }

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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all" + "?order=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[1].title", is("Pride and Prejudice")))
                .andExpect(jsonPath("$.items[0].title", is("The Great Gatsby")));
    }

    @Test
    void borrowAnyBookItemSuccess() throws Exception {

        userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var response = mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookId + "/borrowingAny?" + "userId=" + userId))
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

    @Test
    void borrowAnyBookItemNotAvailable() throws Exception {

        final var user = userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setUserId(user)
                .setStatus(BookItemStatus.AVAILABLE));

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setUserId(user)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItems = bookItemRepository.findAll();

        bookService.borrowActionBookItemById(bookItems.get(0).getId(), userId, BookItemStatus.IN_PROGRESS);
        bookService.borrowActionBookItemById(bookItems.get(1).getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookId + "/borrowingAny?" + "userId=" + userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

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
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookId + "/borrowingAny?" + "userId=" + userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void borrowAnyBookItemInvalidBookId() throws Exception {

        userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = Utils.getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookId + "/borrowingAny?" + "userId=" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCountOfAvailableBookItems() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));
        bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?bookId=" + bookId + "&status=available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(3)));
    }


    @Test
    void getCountOfAvailableBookItemsNoAvailableItems() throws Exception {

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var bookId = bookRepository.findAll().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?bookId=" + bookId + "&status=available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)));
    }

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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?bookId=" + bookId + "&status=available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)));
    }

    @Test
    void getAllBooksAndBookItems() throws Exception {
        final var user = userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

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

        final var bookId = bookRepository.findAll().get(0).getId();

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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all?order=asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.items[0].bookId", notNullValue()))
                .andExpect(jsonPath("$.items[0].title", is("The Great Gatsby")))
                .andExpect(jsonPath("$.items[0].author", is("F. Scott Fitzgerald")))
                .andExpect(jsonPath("$.items[0].description", is("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")))
                .andExpect(jsonPath("$.items[0].edition", is("3rd Edition")))
                .andExpect(jsonPath("$.items[0].publicationyYear", is(1925)))
                .andExpect(jsonPath("$.items[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$.items[0].deletedAt", nullValue()))
                .andExpect(jsonPath("$.items[0].bookItems[0].bookItemId", notNullValue()))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.id", notNullValue()))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.username", is("Alelxo")))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.name", is("Alex")))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.surname", is("Bur")))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.email", is("efaf@gmail.com")))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.phoneNumber", is("380679920267")))
                .andExpect(jsonPath("$.items[0].bookItems[0].user.address", is("assfasfd")))
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
                .andExpect(jsonPath("$.items[1].publicationyYear", is(1930)))
                .andExpect(jsonPath("$.items[1].createdAt", notNullValue()))
                .andExpect(jsonPath("$.items[1].deletedAt", nullValue()))
                .andExpect(jsonPath("$.items[1].bookItems", is(hasSize(0))))
                .andExpect(jsonPath("$.items[1].bookGenres", is(hasSize(0))));
    }

    @Test
    void getAllBooksAnd100BookItems() throws Exception {
        final var user = userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var bookId = bookRepository.findAll().get(0).getId();

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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all?order=asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items[0].bookItems", hasSize(100)));
    }

    @Test
    void getAllBooksAndBookItemsEmptyResponse() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void getBookById() throws Exception {
        final var user = userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var bookId = bookRepository.findAll().get(0).getId();

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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/" + bookId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId", notNullValue()))
                .andExpect(jsonPath("$.title", is("The Great Gatsby")))
                .andExpect(jsonPath("$.author", is("F. Scott Fitzgerald")))
                .andExpect(jsonPath("$.description", is("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")))
                .andExpect(jsonPath("$.edition", is("3rd Edition")))
                .andExpect(jsonPath("$.publicationyYear", is(1925)))
                .andExpect(jsonPath("$.publicationyYear", notNullValue()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.deletedAt", nullValue()))
                .andExpect(jsonPath("$.bookItems[0].bookItemId", notNullValue()))
                .andExpect(jsonPath("$.bookItems[0].user.id", notNullValue()))
                .andExpect(jsonPath("$.bookItems[0].user.username", is("Alelxo")))
                .andExpect(jsonPath("$.bookItems[0].user.name", is("Alex")))
                .andExpect(jsonPath("$.bookItems[0].user.surname", is("Bur")))
                .andExpect(jsonPath("$.bookItems[0].user.email", is("efaf@gmail.com")))
                .andExpect(jsonPath("$.bookItems[0].user.phoneNumber", is("380679920267")))
                .andExpect(jsonPath("$.bookItems[0].user.address", is("assfasfd")))
                .andExpect(jsonPath("$.bookItems[0].status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.bookItems[0].borrowedAt", notNullValue()))
                .andExpect(jsonPath("$.bookItems[0].returnedAt", nullValue()))
                .andExpect(jsonPath("$.bookItems[1].user", nullValue()))
                .andExpect(jsonPath("$.bookItems[1].status", is("AVAILABLE")))
                .andExpect(jsonPath("$.bookItems[1].borrowedAt", nullValue()))
                .andExpect(jsonPath("$.bookItems[1].returnedAt", nullValue()))
                .andExpect(jsonPath("$.bookGenres[0].genreName", is("Action")));
    }

    @Test
    void getBookByIdNotFound() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "/" + UUID.randomUUID()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void borrowAnyBookItem2ThreadsSuccess() {

        userRepository.save(new User()
                .setUsername("Alelxo")
                .setName("Alex")
                .setSurname("Bur")
                .setEmail("efaf@gmail.com")
                .setPhoneNumber("380679920267")
                .setAddress("assfasfd"));

        bookRepository.save(new Book()
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.")
                .setPublisher("Scribner")
                .setEdition("3rd Edition")
                .setPublicationYear(1925));

        final var userId = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

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
        assertEquals(202,statusCodes.get(0));
        assertEquals(500,statusCodes.get(1));
    }

    @Test
    void updateBookItemBorrow2ThreadsSuccess() throws Exception {

        userRepository.save(new User().setUsername("Alelxo").setName("Alex").setSurname("Bur").setEmail("efaf@gmail.com").setPhoneNumber("380679920267").setAddress("assfasfd"));

        userRepository.save(new User().setUsername("Buton").setName("Maron").setSurname("Bur").setEmail("efaf@gmail.com").setPhoneNumber("380679920267").setAddress("assfasfd"));

        bookRepository.save(new Book().setTitle("The Great Gatsby").setAuthor("F. Scott Fitzgerald").setDescription("A classic novel set in the Roaring Twenties that explores themes of wealth, love, and the American Dream.").setPublisher("Scribner").setEdition("3rd Edition").setPublicationYear(1925));

        final var userIdOne = userRepository.findAll().get(0).getId();
        final var bookId = bookRepository.findAll().get(0).getId();

        bookItemRepository.save(new BookItem().setBookId(bookId).setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        var listOfTreads = getCallables(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userIdOne + "&status=in_progress", 3);

        var statusCodes = threadRunner(2, listOfTreads);

        Collections.sort(statusCodes);

        assertEquals(3, statusCodes.size());
        assertEquals(202, statusCodes.get(0));
        assertEquals(404, statusCodes.get(1));

    }

    private List<Callable<Integer>> getCallables(String url, int count) {
        List<Callable<Integer>> listOfThreads = new ArrayList<>();
        for (int x = 0; x < count; x++) {
            listOfThreads.add(() -> {
                var response = mvc.perform(MockMvcRequestBuilders.patch(url)).andReturn().getResponse();

                return response.getStatus();
            });
        }

        return listOfThreads;
    }
}