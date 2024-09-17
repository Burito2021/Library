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

import static java.util.Collections.emptyList;
import static net.library.tools.Tools.objectToStringConverter;
import static net.library.util.Constants.CORRELATION_ID_HEADER_NAME;
import static net.library.util.HttpUtil.GLOBAL_BASE_URI;
import static net.library.util.HttpUtil.USERS;
import static net.library.util.Utils.getUUID;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username", is(username)))
                .andExpect(jsonPath("$.data[0].surname", is(surname)))
                .andExpect(jsonPath("$.data[0].name", is(name)))
                .andExpect(jsonPath("$.data[0].email", is(email)))
                .andExpect(jsonPath("$.data[0].phoneNumber", is(phoneNumber)))
                .andExpect(jsonPath("$.data[0].address", is(address)));
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
                .andExpect(jsonPath("$.errorMsg", is("mandatory param is missing")));
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
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(103)))
                .andExpect(jsonPath("$.errorMsg", is("Wrong msisdn format")));
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
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(103)))
                .andExpect(jsonPath("$.errorMsg", is("Wrong msisdn format")));
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cid", is(xCorrelationId)));
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cid", is(xCorrelationId)));
    }

    @Test
    void deleteAllUsers() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";

        final var xCorrelationIdGet = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username", is(username)));

        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI + USERS))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .header("X-Correlation-Id", xCorrelationIdGet))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
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
                .andExpect(jsonPath("$.data.username", is(username)))
                .andExpect(jsonPath("$.data.name", is(name)))
                .andExpect(jsonPath("$.data.surname", is(surname)))
                .andExpect(jsonPath("$.data.email", is(email)))
                .andExpect(jsonPath("$.data.phoneNumber", is(phoneNumber)))
                .andExpect(jsonPath("$.data.address", is(address)));
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
                .andExpect(jsonPath("$.cid", is(xCorrelationId)));

        final var user = service.getAllUsers();

        assertEquals(emptyList(), user);
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