package net.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.library.model.entity.Book;
import net.library.model.entity.BookItem;
import net.library.model.entity.Genre;
import net.library.model.entity.User;
import net.library.model.request.BookGenreRequest;
import net.library.model.request.BookRequest;
import net.library.model.request.GenreRequest;
import net.library.repository.*;
import net.library.repository.enums.BookAction;
import net.library.repository.enums.BookItemStatus;
import net.library.service.BookService;
import net.library.tools.Tools;
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

import java.util.List;
import java.util.UUID;

import static net.library.util.HttpUtil.*;
import static net.library.util.Utils.getUUID;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
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
        bookService.deleteAll();
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
        final var description = Tools.randomString(500);
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
        final var description = Tools.randomString(501);
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
        final var publisher = Tools.randomString(1);
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
        final var description = Tools.randomString(45);
        final var publisher = Tools.randomString(100);
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
        final var description = Tools.randomString(23);
        final var publisher = Tools.randomString(101);
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
        final var description = Tools.randomString(45);
        final var publisher = Tools.randomString(12);
        final var edition = Tools.randomString(30);
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
        final var description = Tools.randomString(23);
        final var publisher = Tools.randomString(33);
        final var edition = Tools.randomString(31);
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
    void addGenreSuccess() throws Exception {
        final var genreName = "The Great Gatsby";
        final var xCorrelationId = getUUID();

        final var requestBody = GenreRequest
                .builder()
                .name(genreName)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + GENRES)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id", not(hasLength(0))))
                .andExpect(jsonPath("$.genreName", is(genreName)));
    }

    @Test
    void addGenreDbCheck() throws Exception {
        final var genreName = "The Great Gatsby";
        final var xCorrelationId = getUUID();

        final var requestBody = GenreRequest
                .builder()
                .name(genreName)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + GENRES)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id", not(hasLength(0))))
                .andExpect(jsonPath("$.genreName", is(genreName)));

        final var genre = genreRepository.findAll().get(0);

        assertEquals(genreName, genre.getName());
    }

    @Test
    void addGenreNameAlreadyExists() throws Exception {
        final var genreName = "Fiction";
        final var xCorrelationId = getUUID();
        final var requestBody = new Genre()
                .setName(genreName);

        genreRepository.save(requestBody);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + GENRES)
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
    void addGenreNameNull() throws Exception {
        final var xCorrelationId = getUUID();

        final var requestBody = GenreRequest
                .builder()
                .name(null)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + GENRES)
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
    void addGenreEmptyString() throws Exception {
        final var genreName = "";
        final var xCorrelationId = getUUID();
        final var requestBody = GenreRequest
                .builder()
                .name(genreName)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + GENRES)
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
    void addGenreWithinMinLength() throws Exception {
        final var genreName = Tools.randomString(1);
        final var xCorrelationId = getUUID();
        final var requestBody = GenreRequest
                .builder()
                .name(genreName)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + GENRES)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Test
    void addGenreWithinMaxLength() throws Exception {
        final var genreName = Tools.randomString(50);
        final var xCorrelationId = getUUID();
        final var requestBody = GenreRequest
                .builder()
                .name(genreName)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + GENRES)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated());
    }

    @Test
    void addGenreExceedMaxLengthError() throws Exception {
        final var genreName = Tools.randomString(51);
        final var xCorrelationId = getUUID();
        final var requestBody = GenreRequest
                .builder()
                .name(genreName)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + GENRES)
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
                .andExpect(jsonPath("$.bookId", is(bookId.toString())))
                .andExpect(jsonPath("$.userId", is(nullValue())))
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
    void addBookGenre() throws Exception {
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
        genreRepository.save(new Genre(UUID.randomUUID(), "Test"));

        final var bookId = bookRepository.findAll().get(0).getId();
        final var genreId = genreRepository.findAll().get(0).getId();

        bookService.addBookGenres(new BookGenreRequest(bookId, List.of(genreId)));

        final var requestBody = BookGenreRequest.builder()
                .bookId(bookId)
                .genreId(List.of(genreId))
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + GENRES + "/book")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].bookId", is(bookId.toString())))
                .andExpect(jsonPath("$[0].genreId", is(genreId.toString())));

        final var bookGenre = bookGenresRepository.findAll().get(0);
        assertNotNull(bookGenre.getId());
        assertEquals(genreId, bookGenre.getGenre_id());
        assertEquals(bookId, bookGenre.getBook_id());
    }

    @Test
    void addBookGenreBookIdNullError() throws Exception {
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
        genreRepository.save(new Genre(UUID.randomUUID(), "Test"));

        final var bookId = bookRepository.findAll().get(0).getId();
        final var genreId = genreRepository.findAll().get(0).getId();

        bookService.addBookGenres(new BookGenreRequest(bookId, List.of(genreId)));

        final var requestBody = BookGenreRequest.builder()
                .bookId(null)
                .genreId(List.of(genreId))
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + GENRES + "/book")
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
    void addBookGenreGenreIdIdNullError() throws Exception {
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
        genreRepository.save(new Genre(UUID.randomUUID(), "Test"));

        final var bookId = bookRepository.findAll().get(0).getId();
        final var genreId = genreRepository.findAll().get(0).getId();

        bookService.addBookGenres(new BookGenreRequest(bookId, List.of(genreId)));

        final var requestBody = BookGenreRequest.builder()
                .bookId(bookId)
                .genreId(null)
                .build();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + GENRES + "/book")
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
    void getBookGenreParamCheck() throws Exception {
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
        genreRepository.save(new Genre(UUID.randomUUID(), "Test"));

        final var bookId = bookRepository.findAll().get(0).getId();
        final var genreId = genreRepository.findAll().get(0).getId();

        bookService.addBookGenres(new BookGenreRequest(bookId, List.of(genreId)));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + GENRES + "/book")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].bookId", is(bookId.toString())))
                .andExpect(jsonPath("$.items[0].genreId", is(genreId.toString())));
    }

    @Test
    void addBookItemsBookIdNotExist() throws Exception {
        final var xCorrelationId = getUUID();
        final var requestBody = new BookItem()
                .setBookId(UUID.randomUUID());

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI  + ITEMS)
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

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI  + ITEMS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(102)))
                .andExpect(jsonPath(ERROR_MSG, is("mandatory param error")));
    }

    //
    @Sql("classpath:sql/genre.sql")
    @Test
    void getGenreParamCheck() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + GENRES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id", not(hasLength(0))))
                .andExpect(jsonPath("$.items[0].id", is(notNullValue())))
                .andExpect(jsonPath("$.items[0].genreName", is("Action")));
    }


    @Sql("classpath:sql/genre.sql")
    @Test
    void getAllGenres() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI  + GENRES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.total", is(10)))
                .andExpect(jsonPath("$.items[0].genreName", is("Action")))
                .andExpect(jsonPath("$.items[1].genreName", is("Adventure")))
                .andExpect(jsonPath("$.items[2].genreName", is("Comedy")))
                .andExpect(jsonPath("$.items[3].genreName", is("Drama")))
                .andExpect(jsonPath("$.items[4].genreName", is("Fantasy")))
                .andExpect(jsonPath("$.items[5].genreName", is("Horror")))
                .andExpect(jsonPath("$.items[6].genreName", is("Mystery")))
                .andExpect(jsonPath("$.items[7].genreName", is("Romance")))
                .andExpect(jsonPath("$.items[8].genreName", is("Sci-Fi")))
                .andExpect(jsonPath("$.items[9].genreName", is("Thriller")));
    }

    @Sql("classpath:sql/genre.sql")
    @Test
    void getAllGenresPageSize() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + GENRES + "?size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(2)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.total", is(10)))
                .andExpect(jsonPath("$.items[0].genreName", is("Action")))
                .andExpect(jsonPath("$.items[1].genreName", is("Adventure")));
    }

    @Sql("classpath:sql/genre.sql")
    @Test
    void getAllGenresPageNumber() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI  + GENRES + "?page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(2)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.total", is(10)))
                .andExpect(jsonPath("$.items[0].genreName", is("Comedy")))
                .andExpect(jsonPath("$.items[1].genreName", is("Drama")));
    }

    @Test
    void getAllGenresEmpty() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + GENRES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.items.length()", is(0)));
    }

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

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI  + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=in_progress"))
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

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI  + ITEMS + "/" + bookItemId  + "/borrowing?" +  "userId=" + userId + "&status=removed"))
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

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI  + ITEMS + "/" + bookItemId  + "/borrowing?" +  "userId=" + userId + "&status=in_progress"))
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

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI  + ITEMS + "/" + bookItemId + "/borrowing?" + "userId=" + userId + "&status=in_progress")
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

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI  + ITEMS + "/" + bookItemId  + "/borrowing?" +  "userId=" + userId + "&status=removal")
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

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI  + ITEMS + "/" + null  + "/borrowing?" +  "userId=" + userId + "&status=in_progress")
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

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId  + "/borrowing?" +  "userId=" + null + "&status=in_progress")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath(CID, is(xCorrelationId)))
                .andExpect(jsonPath(ERROR_ID, is(110)))
                .andExpect(jsonPath(ERROR_MSG, is("wrong type format")));
    }

    @Test
    void updateBookItemReturn() throws Exception {

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
                .setUserId(userId)
                .setStatus(BookItemStatus.IN_PROGRESS));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId))
                .andExpect(status().isAccepted());

        final var bookItemAfterUpdate = bookItemRepository.findAll().get(0);

        assertEquals(BookItemStatus.AVAILABLE, bookItemAfterUpdate.getStatus());
    }

    @Test
    void updateBookItemReturnHiDbHistoryCheck() throws Exception {

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
                .setUserId(userId)
                .setStatus(BookItemStatus.IN_PROGRESS));

        final var bookItemId = bookItemRepository.findAll().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId))
                .andExpect(status().isAccepted());

        final var bookHistory = bookItemHistoryRepository.findAll();
        assertEquals(1, bookHistory.size());
        assertNotNull(bookHistory.get(0).getId());
        assertEquals(bookItemId, bookHistory.get(0).getItemId());
        assertEquals(userId, bookHistory.get(0).getUserId());
        assertEquals(BookAction.RETURNED, bookHistory.get(0).getActionType());
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

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI  + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBookItemReturnWrongBookId() throws Exception {

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
                .setUserId(userId)
                .setStatus(BookItemStatus.AVAILABLE));

        final var bookItemId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI  + ITEMS + "/" + bookItemId + "/return?" + "userId=" + userId))
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

        bookService.updateBookItemBorrow(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.IN_PROGRESS);

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

        bookService.updateBookItemBorrow(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.AVAILABLE);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?status=available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].bookItemId", notNullValue()))
                .andExpect(jsonPath("$.items[0].bookItemId", not(hasLength(0))))
                .andExpect(jsonPath("$.items[0].bookId", notNullValue()))
                .andExpect(jsonPath("$.items[0].bookId", not(hasLength(0))))
                .andExpect(jsonPath("$.items[0].userId", is(userId.toString())))
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

        bookService.updateBookItemBorrow(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI  + ITEMS + "?status=available"))
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

        bookService.updateBookItemBorrow(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.REMOVED);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI  + ITEMS + "?status=removed"))
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

        bookService.updateBookItemBorrow(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI  + ITEMS + "?status=in_progress"))
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

        bookService.updateBookItemBorrow(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI  + ITEMS + "?bookItemId=" + bookItemId + "&status=in_progress"))
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

        bookService.updateBookItemBorrow(bookItemRepository.findAll().get(0).getId(), userId, BookItemStatus.IN_PROGRESS);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ITEMS + "?sortBy=status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(4)));
    }

    @Test
    void getAllAvailableNoItemsNoFilters() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI  + ITEMS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(0)));
    }

    @Sql("classpath:sql/data.sql")
    @Test
    void getAllBooks() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.total", is(10)))
                .andExpect(jsonPath("$.items.length()", is(10)));
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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id", notNullValue()))
                .andExpect(jsonPath("$.items[0].id", not(hasLength(0))))
                .andExpect(jsonPath("$.items[0].title", is(title)))
                .andExpect(jsonPath("$.items[0].author", is(author)))
                .andExpect(jsonPath("$.items[0].description", is(description)))
                .andExpect(jsonPath("$.items[0].publisher", is(publisher)))
                .andExpect(jsonPath("$.items[0].edition", is(edition)))
                .andExpect(jsonPath("$.items[0].publication", is(publicationYear)));
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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "?sortBy=title"))
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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "?sortBy=title&order=asc"))
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

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + BOOKS + "?order=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[1].title", is("Pride and Prejudice")))
                .andExpect(jsonPath("$.items[0].title", is("The Great Gatsby")));
    }
}