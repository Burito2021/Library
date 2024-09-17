package net.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.library.model.entity.User;
import net.library.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static net.library.exception.Constants.CORRELATION_ID_HEADER_NAME;
import static net.library.util.HttpUtil.GLOBAL_BASE_URI;
import static net.library.util.HttpUtil.USERS;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService service;

    @Autowired
    public UserControllerTest(UserService service) {
        this.service = service;
    }

    @BeforeEach
    void clean(){
        service.deleteAll();
    }

    @Test
    void getAllUsers() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf";
        final var phoneNumber = "1212121";
        final var address = "assfasfd";
        final var xCorrelationId = UUID.randomUUID().toString();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content("{ \"username\": \"Alelxo\", \"surname\": \"Bur\", " +
                                "\"name\": \"Alex\", \"email\": \"efaf\", \"phoneNumber\": \"1212121\", " +
                                "\"address\": \"assfasfd\"}"))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].username",is(username)))
                .andExpect(jsonPath("$.users[0].surname",is(surname)))
                .andExpect(jsonPath("$.users[0].name",is(name)))
                .andExpect(jsonPath("$.users[0].email",is(email)))
                .andExpect(jsonPath("$.users[0].phoneNumber",is(phoneNumber)))
                .andExpect(jsonPath("$.users[0].address",is(address)));
    }

    @Test
    void addUser() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf";
        final var phoneNumber = "1212121";
        final var address = "assfasfd";
        final var xCorrelationId = UUID.randomUUID().toString();
        var user = new User();
        user.setId(null);
        user.setUsername(username);
        user.setSurname(surname);
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content("{ \"username\": \"Book Title\", \"surname\": \"Book Author\", " +
                                "\"name\": \"Book Author\", \"email\": \"asdas\", \"phoneNumber\": " +
                                "\"address\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cid", is(xCorrelationId)));
    }

    @Test
    void deleteAllUsers() throws Exception {
        final var xCorrelationId = UUID.randomUUID().toString();
        final var xCorrelationIdGet = UUID.randomUUID().toString();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content("{ \"username\": \"Book Title\", \"surname\": \"Book Author\", " +
                                "\"name\": \"Book Author\", \"email\": \"asdas\", \"phoneNumber\": " +
                                "\"address\"}"))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].username",is("Book Title")));

        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI+ USERS))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .header("X-Correlation-Id",xCorrelationIdGet))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.x-Correlation-id",is(xCorrelationIdGet)))
                .andExpect(jsonPath("$.users",hasSize(0)));
    }

    @Test
    void getUsersError() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/fg"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(125)))
                .andExpect(jsonPath("$.errorMsg", is("global error")));
    }
}