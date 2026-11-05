package net.library.controller;

import com.jayway.jsonpath.JsonPath;
import net.library.TestContainers;
import net.library.model.entity.Book;
import net.library.model.entity.BookItem;
import net.library.model.entity.BookItemHistory;
import net.library.repository.BookItemHistoryRepository;
import net.library.repository.BookItemRepository;
import net.library.repository.BookRepository;
import net.library.repository.UserRepository;
import net.library.repository.enums.BookAction;
import net.library.repository.enums.BookItemStatus;
import net.library.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static net.library.tools.HttpUtil.ADMIN_USER;
import static net.library.tools.HttpUtil.COMMON_USER;
import static net.library.tools.HttpUtil.FINGERPRINT;
import static net.library.tools.HttpUtil.PASSWORD_ADMIN;
import static net.library.tools.HttpUtil.PASSWORD_USER;
import static net.library.tools.Tools.getAccessToken;
import static net.library.util.HttpUtil.ANALYTICS;
import static net.library.util.HttpUtil.AUTHORIZATION;
import static net.library.util.HttpUtil.BEARER;
import static net.library.util.HttpUtil.GLOBAL_BASE_URI;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AnalyticsControllerIntegrationTest extends TestContainers {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookItemRepository bookItemRepository;
    @Autowired
    private BookItemHistoryRepository bookItemHistoryRepository;
    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanAfter() {
        bookItemHistoryRepository.deleteAll();
        bookItemRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Book saveBook(String title, String author, int publicationYear) {
        return bookRepository.save(new Book()
                .setTitle(title)
                .setAuthor(author)
                .setDescription("description")
                .setPublisher("publisher")
                .setEdition("1st Edition")
                .setPublicationYear(publicationYear));
    }

    private BookItem saveItem(java.util.UUID bookId) {
        return bookItemRepository.save(new BookItem()
                .setBookId(bookId)
                .setStatus(BookItemStatus.AVAILABLE));
    }

    private void recordBorrow(java.util.UUID itemId, LocalDateTime actionAt) {
        recordAction(itemId, BookAction.BORROWED, actionAt);
    }

    private void recordAction(java.util.UUID itemId, BookAction action, LocalDateTime actionAt) {
        bookItemHistoryRepository.save(new BookItemHistory()
                .setItemId(itemId)
                .setActionType(action)
                .setActionAt(actionAt));
    }

    // created_at is filled by the database on insert, so it is set by a second save, before any copies exist.
    private Book saveBookCreatedAt(String title, LocalDateTime createdAt) {
        return bookRepository.save(saveBook(title, "Author", 2000).setCreatedAt(createdAt));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getMostBorrowedBooksReturnsRankedResults() throws Exception {
        final var gatsby = saveBook("The Great Gatsby", "F. Scott Fitzgerald", 1925);
        final var mockingbird = saveBook("To Kill a Mockingbird", "Harper Lee", 1960);

        final var gatsbyItem = saveItem(gatsby.getId());
        final var mockingbirdItem = saveItem(mockingbird.getId());

        recordBorrow(gatsbyItem.getId(), LocalDateTime.now().minusDays(1));
        recordBorrow(gatsbyItem.getId(), LocalDateTime.now().minusDays(2));
        recordBorrow(mockingbirdItem.getId(), LocalDateTime.now().minusDays(3));

        final var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.items[0].title", is("The Great Gatsby")))
                .andExpect(jsonPath("$.items[0].borrowCount", is(2)))
                .andExpect(jsonPath("$.items[1].title", is("To Kill a Mockingbird")))
                .andExpect(jsonPath("$.items[1].borrowCount", is(1)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getMostBorrowedBooksFiltersByPublicationYear() throws Exception {
        final var oldBook = saveBook("Old Book", "Author One", 1900);
        final var newBook = saveBook("New Book", "Author Two", 2020);

        final var oldItem = saveItem(oldBook.getId());
        final var newItem = saveItem(newBook.getId());

        recordBorrow(oldItem.getId(), LocalDateTime.now());
        recordBorrow(newItem.getId(), LocalDateTime.now());

        final var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                        .param("publicationYearFrom", "2000")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items[0].title", is("New Book")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getMostBorrowedBooksReturnsEmptyWhenNoBorrows() throws Exception {
        saveBook("Unborrowed Book", "Some Author", 2010);

        final var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getMostBorrowedBooksForbiddenForNonAdmin() throws Exception {
        final var accessToken = getAccessToken(authService, COMMON_USER, PASSWORD_USER, FINGERPRINT);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isForbidden());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getCountersReturnsAggregatedCounts() throws Exception {
        final var book1 = saveBook("Book One", "Author A", 2000);
        final var book2 = saveBook("Book Two", "Author B", 2005);
        final var item1 = saveItem(book1.getId());
        final var item2 = saveItem(book2.getId());

        recordBorrow(item1.getId(), LocalDateTime.now());
        recordBorrow(item2.getId(), LocalDateTime.now());
        recordBorrow(item2.getId(), LocalDateTime.now());

        final var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/counters")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks", is(2)))
                .andExpect(jsonPath("$.totalAuthors", is(2)))
                .andExpect(jsonPath("$.totalBorrows", is(3)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getCountersForbiddenForNonAdmin() throws Exception {
        final var accessToken = getAccessToken(authService, COMMON_USER, PASSWORD_USER, FINGERPRINT);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/counters")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isForbidden());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getMostBorrowedBooksReportsTotalFromCountQueryWhenFirstPageIsFull() throws Exception {
        // Book A has two borrows, so a count query that counted borrows instead of books would report 4, not 3.
        final var bookAItem = saveItem(saveBook("Book A", "Author", 2000).getId());
        recordBorrow(bookAItem.getId(), LocalDateTime.now());
        recordBorrow(bookAItem.getId(), LocalDateTime.now());
        for (var title : List.of("Book B", "Book C")) {
            recordBorrow(saveItem(saveBook(title, "Author", 2000).getId()).getId(), LocalDateTime.now());
        }

        final var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                        .param("size", "2")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(3)))
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getMostBorrowedBooksShowsEveryTiedBookExactlyOnceAcrossPages() throws Exception {
        // 30 books with one borrow each, read 2 per page. Without a tie-breaker in ORDER BY, PostgreSQL sorts
        // only the rows each page needs, so tied books come back in a different order per page: in a trial
        // run 6 books were repeated and 6 never shown. A smaller data set does not reproduce it.
        final List<String> titles = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            final var title = "Tied Book " + i;
            recordBorrow(saveItem(saveBook(title, "Author", 2000).getId()).getId(), LocalDateTime.now());
            titles.add(title);
        }
        final int pageSize = 2;

        final var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);

        final List<String> shown = new ArrayList<>();
        for (int page = 0; page < titles.size() / pageSize; page++) {
            final var body = mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                            .param("size", String.valueOf(pageSize))
                            .param("page", String.valueOf(page))
                            .header(AUTHORIZATION, BEARER + accessToken))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            shown.addAll(JsonPath.<List<String>>read(body, "$.items[*].title"));
        }

        assertEquals(titles.stream().sorted().toList(), shown.stream().sorted().toList());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getMostBorrowedBooksAddsUpBorrowsOfAllCopiesOfABook() throws Exception {
        final var book = saveBook("Many Copies", "Author", 2000);
        final var firstCopy = saveItem(book.getId());
        final var secondCopy = saveItem(book.getId());

        recordBorrow(firstCopy.getId(), LocalDateTime.now());
        recordBorrow(secondCopy.getId(), LocalDateTime.now());
        recordBorrow(secondCopy.getId(), LocalDateTime.now());

        final var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items[0].title", is("Many Copies")))
                .andExpect(jsonPath("$.items[0].borrowCount", is(3)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void borrowCountsIgnoreAddedAndReturnedHistory() throws Exception {
        final var item = saveItem(saveBook("Borrowed Once", "Author", 2000).getId());
        final var neverBorrowedItem = saveItem(saveBook("Never Borrowed", "Author", 2000).getId());

        recordAction(item.getId(), BookAction.ADDED, LocalDateTime.now());
        recordAction(item.getId(), BookAction.BORROWED, LocalDateTime.now());
        recordAction(item.getId(), BookAction.RETURNED, LocalDateTime.now());
        recordAction(neverBorrowedItem.getId(), BookAction.ADDED, LocalDateTime.now());

        final var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items[0].title", is("Borrowed Once")))
                .andExpect(jsonPath("$.items[0].borrowCount", is(1)));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/counters")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBorrows", is(1)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getMostBorrowedBooksIncludesBorrowsExactlyOnTheActionDateBoundaries() throws Exception {
        final var item = saveItem(saveBook("Boundary Book", "Author", 2000).getId());

        recordBorrow(item.getId(), LocalDateTime.of(2024, 1, 1, 9, 59));
        recordBorrow(item.getId(), LocalDateTime.of(2024, 1, 1, 10, 0));
        recordBorrow(item.getId(), LocalDateTime.of(2024, 1, 1, 12, 0));
        recordBorrow(item.getId(), LocalDateTime.of(2024, 1, 1, 12, 1));

        final var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                        .param("actionFrom", "2024-01-01T10:00")
                        .param("actionTo", "2024-01-01T12:00")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items[0].borrowCount", is(2)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getMostBorrowedBooksIncludesBooksCreatedExactlyOnTheCreatedDateBoundaries() throws Exception {
        final var before = saveBookCreatedAt("Created Before", LocalDateTime.of(2019, 12, 31, 23, 59));
        final var onFrom = saveBookCreatedAt("Created On From", LocalDateTime.of(2020, 1, 1, 0, 0));
        final var onTo = saveBookCreatedAt("Created On To", LocalDateTime.of(2020, 12, 31, 0, 0));
        final var after = saveBookCreatedAt("Created After", LocalDateTime.of(2020, 12, 31, 0, 1));
        for (var book : List.of(before, onFrom, onTo, after)) {
            recordBorrow(saveItem(book.getId()).getId(), LocalDateTime.now());
        }

        final var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                        .param("createdFrom", "2020-01-01T00:00")
                        .param("createdTo", "2020-12-31T00:00")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.items[*].title",
                        containsInAnyOrder("Created On From", "Created On To")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getMostBorrowedBooksRejectsInvalidFiltersThroughTheApplicationErrorHandler() throws Exception {
        final var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN, FINGERPRINT);

        for (var param : List.of(
                new String[]{"publicationYearFrom", "not-a-number"},
                new String[]{"actionFrom", "01.01.2024"})) {
            mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                            .param(param[0], param[1])
                            .header(AUTHORIZATION, BEARER + accessToken))
                    .andExpect(status().isBadRequest());
        }

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + ANALYTICS + "/most-borrowed-books")
                        .param("publicationYearFrom", "2000")
                        .param("publicationYearTo", "1900")
                        .header(AUTHORIZATION, BEARER + accessToken))
                .andExpect(status().isBadRequest());
    }
}
