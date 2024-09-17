package net.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.library.dto.AddUser;
import net.library.model.request.UserRequest;
import net.library.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static net.library.tools.Tools.objectToStringConverter;
import static net.library.util.HttpUtil.*;
import static net.library.util.Utils.getUUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService service;

    @BeforeEach
    void clean(){
        service.deleteAll();
    }

    @Test
    void getAllUsers() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username", is(username)))
                .andExpect(jsonPath("$[0].surname", is(surname)))
                .andExpect(jsonPath("$[0].name", is(name)))
                .andExpect(jsonPath("$[0].email", is(email)))
                .andExpect(jsonPath("$[0].phoneNumber", is(phoneNumber)))
                .andExpect(jsonPath("$[0].address", is(address)));
    }

    @Test
    void excessiveLength13MsisdnValidator() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "3806799202671";
        final var address = "assfasfd";

        final var requestBody = new AddUser()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Test
    void lengthMsisdnLessThan10Validator() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920";
        final var address = "assfasfd";

        final var requestBody = new AddUser()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Test
    void msisdnCheckWrongFormat80() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "80679920267";
        final var address = "assfasfd";

        final var requestBody = new AddUser()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Test
    void msisdnCheckLength10() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "3806799202";
        final var address = "assfasfd";

        final var requestBody = new AddUser()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Test
    void usernameValidator() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "3806799202";
        final var address = "assfasfd";

        final var requestBody = new AddUser()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(104)))
                .andExpect(jsonPath("$.errorMsg", is("Username already exists in Db")));
    }

    @Test
    void addUser() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";

        final var requestBody = new AddUser()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Test
    void deleteAllUsers() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var userId = service.getAllUsers();

        assertFalse(userId.isEmpty());

        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI + USERS))
                .andExpect(status().isOk());

        final var user = service.getAllUsers();

        assertTrue(user.isEmpty());
    }

    @Test
    void findUserById() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";

        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var userId = service.getAllUsers().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/" + userId)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(username)))
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.surname", is(surname)))
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.phoneNumber", is(phoneNumber)))
                .andExpect(jsonPath("$.address", is(address)));
    }

    @Test
    void deleteById() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";

        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var userId = service.getAllUsers().get(0).getId();

        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI + USERS + "/" + userId)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var user = service.getAllUsers();

        assertTrue(user.isEmpty());
    }

    @Test
    void getUsersError() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/fg"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(101)))
                .andExpect(jsonPath("$.errorMsg", is("global error")));
    }
}