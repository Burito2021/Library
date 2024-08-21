package net.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static net.library.util.HttpUtil.BASE_URI;
import static net.library.util.HttpUtil.BOOKS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookService service;

    @Autowired
    public BookControllerTest(BookService service) {
        this.service = service;
    }

    @Test
    void getBooks() throws Exception {
        service.deleteAllBooks();

        mvc.perform(MockMvcRequestBuilders.post(BASE_URI + BOOKS)
                        .contentType("application/json")
                        .content("{ \"title\": \"Book Title\", \"author\": \"Book Author\" }"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.cid", notNullValue()));

        mvc.perform(MockMvcRequestBuilders.get(BASE_URI + BOOKS))
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Content-Type", "application/json"))
                .andExpect(jsonPath("$[0].book", notNullValue()))
                .andExpect((jsonPath("$[0].title", is("Book Title"))))
                .andExpect(jsonPath("$[0].author", is("Book Author")));
    }

    @Test
    void addBook() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(BASE_URI + BOOKS)
                        .contentType("application/json")
                        .content("{ \"title\": \"Book Title\", \"author\": \"Book Author\" }"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.cid", notNullValue()));
    }

    @Test
    void addBookTitleBadRequestError() throws Exception {

        mvc.perform(MockMvcRequestBuilders.post(BASE_URI + BOOKS)
                        .contentType("application/json")
                        .content("{ \"title\": \"\", \"author\": \"Book Author\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(121)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param is missing")));
    }

    @Test
    void addBookAuthorBadRequestError() throws Exception {

        mvc.perform(MockMvcRequestBuilders.post(BASE_URI + BOOKS)
                        .contentType("application/json")
                        .content("{ \"title\": \"ds\", \"author\": \"\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(121)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param is missing")));
    }
}