package net.library.controller;

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
import static org.hamcrest.Matchers.is;
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
        bookItemHistoryRepository.save(new BookItemHistory()
                .setItemId(itemId)
                .setActionType(BookAction.BORROWED)
                .setActionAt(actionAt));
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
}
