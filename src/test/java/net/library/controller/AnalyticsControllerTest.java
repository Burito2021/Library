package net.library.controller;

import net.library.model.request.MostBorrowedBooksFilter;
import net.library.model.response.AnalyticsCountersResponse;
import net.library.model.response.MostBorrowedBookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @InjectMocks
    private AnalyticsController analyticsController;
    @Mock
    private net.library.service.LibraryAnalyticsService service;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(analyticsController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getMostBorrowedBooksReturnsPagedResult() throws Exception {
        final var bookId = UUID.randomUUID();
        final var pageable = PageRequest.of(0, 10);
        final var response = new MostBorrowedBookResponse(bookId, "The Great Gatsby", "F. Scott Fitzgerald", 1925, 5L);

        when(service.getMostBorrowedBooks(any(), any()))
                .thenReturn(new PageImpl<>(List.of(response), pageable, 1));

        mvc.perform(get("/analytics/most-borrowed-books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.items[0].bookId", is(bookId.toString())))
                .andExpect(jsonPath("$.items[0].title", is("The Great Gatsby")))
                .andExpect(jsonPath("$.items[0].author", is("F. Scott Fitzgerald")))
                .andExpect(jsonPath("$.items[0].publicationYear", is(1925)))
                .andExpect(jsonPath("$.items[0].borrowCount", is(5)));
    }

    @Test
    void getMostBorrowedBooksPassesFilterParamsToService() throws Exception {
        final var pageable = PageRequest.of(0, 10);
        when(service.getMostBorrowedBooks(any(), any()))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mvc.perform(get("/analytics/most-borrowed-books")
                        .param("actionFrom", "2024-01-01T00:00")
                        .param("actionTo", "2024-12-31T00:00")
                        .param("createdFrom", "2020-01-01T00:00")
                        .param("createdTo", "2020-12-31T00:00")
                        .param("publicationYearFrom", "1900")
                        .param("publicationYearTo", "2000"))
                .andExpect(status().isOk());

        verify(service).getMostBorrowedBooks(
                eq(new MostBorrowedBooksFilter(
                        LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 12, 31, 0, 0),
                        LocalDateTime.of(2020, 1, 1, 0, 0), LocalDateTime.of(2020, 12, 31, 0, 0),
                        1900, 2000)),
                any()
        );
    }

    @Test
    void getMostBorrowedBooksRejectsInvalidPublicationYear() throws Exception {
        mvc.perform(get("/analytics/most-borrowed-books")
                        .param("publicationYearFrom", "not-a-number"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getMostBorrowedBooksRejectsInvalidDate() throws Exception {
        mvc.perform(get("/analytics/most-borrowed-books")
                        .param("actionFrom", "01.01.2024"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getMostBorrowedBooksRejectsReversedRange() throws Exception {
        mvc.perform(get("/analytics/most-borrowed-books")
                        .param("publicationYearFrom", "2000")
                        .param("publicationYearTo", "1900"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getMostBorrowedBooksReturnsEmptyPageWhenNoResults() throws Exception {
        final var pageable = PageRequest.of(0, 10);
        when(service.getMostBorrowedBooks(any(), any()))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mvc.perform(get("/analytics/most-borrowed-books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void getCountersReturnsCounters() throws Exception {
        when(service.getCounters()).thenReturn(new AnalyticsCountersResponse(42L, 7L, 123L));

        mvc.perform(get("/analytics/counters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks", is(42)))
                .andExpect(jsonPath("$.totalAuthors", is(7)))
                .andExpect(jsonPath("$.totalBorrows", is(123)));
    }
}
