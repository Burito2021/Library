package net.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.library.config.JasyptEncryptorConfig;
import net.library.model.entity.User;
import net.library.model.request.UserRequest;
import net.library.repository.enums.ModerationState;
import net.library.repository.enums.RoleType;
import net.library.repository.enums.UserState;
import net.library.service.UserService;
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

import static net.library.tools.Tools.objectToStringConverter;
import static net.library.util.HttpUtil.*;
import static net.library.util.Utils.getUUID;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService service;

    @AfterEach
    void clean() {
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
                .andExpect(jsonPath("$.items[0].username", is(username)))
                .andExpect(jsonPath("$.items[0].surname", is(surname)))
                .andExpect(jsonPath("$.items[0].name", is(name)))
                .andExpect(jsonPath("$.items[0].email", is(email)))
                .andExpect(jsonPath("$.items[0].phoneNumber", is(phoneNumber)))
                .andExpect(jsonPath("$.items[0].address", is(address)));
    }

    @Test
    void getAllUsersNoUsersInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void getUsersError() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/fg"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void maxPageSizeHas100() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?size=101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(100)));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void defaultPageSizeHas10() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(10)));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void userNameFilterLessThan3() throws Exception {
        final var xCorrelation = Utils.getUUID();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?username=ad")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelation))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(CID, is(xCorrelation)))
                .andExpect(jsonPath(ERROR_ID, is(105)))
                .andExpect(jsonPath(ERROR_MSG, is("Filter length should be more that 2 letters")));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void userNameFilterMoreThan2() throws Exception {
        final var xCorrelation = Utils.getUUID();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?username=user_99")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void userNameFilterSeveralReturnValues() throws Exception {
        final var xCorrelation = Utils.getUUID();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?username=user_9")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(10)));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void userNameFilterNoValuesSpecified() throws Exception {
        final var xCorrelation = Utils.getUUID();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?username=")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.total", is(101)))
                .andExpect(jsonPath("$.items", hasSize(10)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void userNamePageCheck() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user_3")))
                .andExpect(jsonPath("$.items[1].username", is("user_2")))
                .andExpect(jsonPath("$.items", hasSize(2)));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user_1")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void userNameFilterStartDate() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?start_time=2024-10-19T00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user_3")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void userNameFilterEndDate() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?end_time=2024-10-18T11:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user_2")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void userNameFilterStartAndEndDate() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?start_time=2024-10-17T22:00&end_time=2024-10-18T22:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user_1")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void userNameFilterStartDateEmptyAndSortingOrderDefaultDesc() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?start_time=&end_time=2025-10-19T23:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user_3")))
                .andExpect(jsonPath("$.items[1].username", is("user_2")))
                .andExpect(jsonPath("$.items[2].username", is("user_1")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortDirectionAsc() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?order=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user_1")))
                .andExpect(jsonPath("$.items[1].username", is("user_2")))
                .andExpect(jsonPath("$.items[2].username", is("user_3")))

                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortCustomOrderDesc() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?order=desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user_3")))
                .andExpect(jsonPath("$.items[1].username", is("user_2")))
                .andExpect(jsonPath("$.items[2].username", is("user_1")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortByFieldEmailOrderDefault() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?sortBy=email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].email", is("user_3@example.com")))
                .andExpect(jsonPath("$.items[1].email", is("user_2@example.com")))
                .andExpect(jsonPath("$.items[2].email", is("user_1@example.com")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortByFieldNameOrderDefault() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?sortBy=name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name", is("Name_3")))
                .andExpect(jsonPath("$.items[1].name", is("Name_2")))
                .andExpect(jsonPath("$.items[2].name", is("Name_1")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortByFieldSurnameCustomOrderAsc() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?sortBy=surname&order=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].surname", is("Aurname")))
                .andExpect(jsonPath("$.items[1].surname", is("Burname")))
                .andExpect(jsonPath("$.items[2].surname", is("Surname")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortByFieldPhoneNumberOrderDefault() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?sortBy=phoneNumber"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].phoneNumber", is("380679920203")))
                .andExpect(jsonPath("$.items[1].phoneNumber", is("380679920202")))
                .andExpect(jsonPath("$.items[2].phoneNumber", is("380679920201")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortByFieldAddressCustomOrderAsc() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?sortBy=address&order=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].address", is("Street 151, City 31, State 39")))
                .andExpect(jsonPath("$.items[1].address", is("Street 561, City 82, State 27")))
                .andExpect(jsonPath("$.items[2].address", is("Street 688, City 90, State 49")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByModerationStateOnReviewLowerCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(MODERATION_STATE, "on_review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByModerationStateApprovedUpperCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(MODERATION_STATE, "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByModerationStateDeclineUpperCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(MODERATION_STATE, "Declined"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByUserStateActiveLowerCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(USER_STATE, "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(5)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByUserStateSuspendedUpperCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(USER_STATE, "SUSPENDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByUserStateBannedLowerCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(USER_STATE, "banned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByRoleTypeUserLowerCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(6)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByRoleTypeAdminUpperCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(ROLE, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void getUserByRoleTypeAdminNotExistInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(ROLE, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }


    @Sql("classpath:sql/3_records.sql")
    @Test
    public void getUserByModerationStateEmptyValue() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(MODERATION_STATE, ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void getUserByUserStateEmptyValue() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(USER_STATE, ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void getUserByRoleTypeEmptyValue() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .queryParam(ROLE, ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Test
    void excessiveLength13MsisdnValidator() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "3806799202671";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

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
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

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
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

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
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Test
    void usernameValidatorAlreadyExistsInDb() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "38067992021";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

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
    void usernameValidatorEmptyString() throws Exception {
        final var username = "";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "38067992021";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

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
    void usernameValidatorNull() throws Exception {
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "38067992021";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(null)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

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
    void addUserWrongEmailFormat() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
        ;
    }

    @Test
    void addUserEmptySurName() throws Exception {
        final var username = "Alelxo";
        final var surname = "";
        final var name = "Alex";
        final var email = "efaf";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
        ;
    }

    @Test
    void addUserNullSurName() throws Exception {
        final var username = "Alelxo";
        final var name = "Alex";
        final var email = "efaf";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(null)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Test
    void addUserNullName() throws Exception {
        final var username = "Alelxo";
        final var surname = "Alelxo";
        final var email = "efaf";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(null)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Test
    void addUserEmptyName() throws Exception {
        final var username = "Alelxo";
        final var surname = "Alelxo";
        final var name = "";
        final var email = "efaf";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Test
    void addUserEmptyEmail() throws Exception {
        final var username = "Alelxo";
        final var surname = "Alelxo";
        final var name = "asda";
        final var email = "";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Test
    void addUserNullEmail() throws Exception {
        final var username = "Alelxo";
        final var surname = "Alelxo";
        final var name = "asda";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(null)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Test
    void addUserEmptyPhoneNumber() throws Exception {
        final var username = "Alelxo";
        final var surname = "Alelxo";
        final var name = "asda";
        final var email = "fasfas@gmail.com";
        final var phoneNumber = "";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Test
    void addUserNullPhoneNumber() throws Exception {
        final var username = "Alelxo";
        final var surname = "Alelxo";
        final var name = "asda";
        final var email = "fasfas@gmail.com";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(null)
                .setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Test
    void addUserNullAddress() throws Exception {
        final var username = "Alelxo";
        final var surname = "Alelxo";
        final var name = "asda";
        final var email = "fasfas@gmail.com";
        final var phoneNumber = "380679920267";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(null);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(status().isCreated());
    }

    @Test
    void addUserEmptyAddress() throws Exception {
        final var username = "Alelxo";
        final var surname = "Alelxo";
        final var name = "asda";
        final var email = "fasfas@gmail.com";
        final var phoneNumber = "380679920267";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress("");

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(status().isCreated());
    }

    @Test
    void addUser() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(username)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.id", not(hasLength(0))))
                .andExpect(jsonPath("$.username", is(username)))
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.surname", is(surname)))
                .andExpect(jsonPath("$.phoneNumber", is(phoneNumber)))
                .andExpect(jsonPath("$.address", is(address)));
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
                .andExpect(status().isNoContent());

        final var user = service.getAllUsers();

        assertTrue(user.isEmpty());
    }

    @Test
    void deleteAllUsersIfNoUsersExist() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI + USERS))
                .andExpect(status().isNoContent());

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
    void findUserByIdWhenNoUserExists() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/" + getUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findUserByNotUUID() throws Exception {
        final var xCorrelation = getUUID();
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/" + "1212")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelation))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", is(xCorrelation)))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
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
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var user = service.getAllUsers();

        assertTrue(user.isEmpty());
    }

    @Test
    void deleteByIdIfNoUserExistByUUID() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI + USERS + "/" + getUUID()))
                .andExpect(status().isNotFound());

        final var user = service.getAllUsers();

        assertTrue(user.isEmpty());
    }

    @Test
    void deleteByIdIfNoUserD() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI + USERS + "/" + "121"))
                .andExpect(status().isBadRequest())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", is(notNullValue())))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
        ;
    }

    @Test
    void updateModerationStateUserNotFound() throws Exception {
        final var userId = getUUID();
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "moderation")
                        .queryParam("state", "approved")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Test
    void updateModerationStateApprovedSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var user = service.getAllUsers()
                .get(0);

        final var userId = user.getId();

        assertEquals(ModerationState.ON_REVIEW, user.getModerationState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "moderation")
                        .queryParam("state", "approved")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userModStateAfterUpdate = service.getAllUsers()
                .get(0).getModerationState();
        assertEquals(ModerationState.APPROVED, userModStateAfterUpdate);
    }

    @Test
    void updateModerationStateDeclinedSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var user = service.getAllUsers()
                .get(0);

        final var userId = user.getId();

        assertEquals(ModerationState.ON_REVIEW, user.getModerationState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "moderation")
                        .queryParam("state", "declined")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userModStateAfterUpdate = service.getAllUsers()
                .get(0).getModerationState();
        assertEquals(ModerationState.DECLINED, userModStateAfterUpdate);
    }

    @Test
    void updateModerationStateWrongState() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var user = service.getAllUsers()
                .get(0);

        final var userId = user.getId();

        assertEquals(ModerationState.ON_REVIEW, user.getModerationState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "moderation")
                        .queryParam("state", "banned")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
    }

    @Test
    void updateUserStateUserNotFound() throws Exception {
        final var userId = getUUID();
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "ACTIVE")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Test
    void updateRoleTypeNotFound() throws Exception {
        final var userId = getUUID();
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "role")
                        .queryParam("type", "ADMIN")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Test
    void updateUserStateBannedSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var user = service.getAllUsers().get(0);
        final var userId = user.getId();

        assertEquals(UserState.ACTIVE, user.getUserState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "BANNED")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userModStateAfterUpdate = service.getAllUsers()
                .get(0).getUserState();
        assertEquals(UserState.BANNED, userModStateAfterUpdate);
    }

    @Test
    void updateUserStateSuspendedSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var user = service.getAllUsers().get(0);

        final var userId = user.getId();

        assertEquals(UserState.ACTIVE, user.getUserState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "SUSPENDED")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userModStateAfterUpdate = service.getAllUsers()
                .get(0).getUserState();
        assertEquals(UserState.SUSPENDED, userModStateAfterUpdate);
    }

    @Test
    void updateUserStateActiveSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var userId = service.getAllUsers()
                .get(0).getId();

        service.updateUserState(userId, UserState.BANNED);

        final var userBeforeUpdate = service.getAllUsers().get(0);

        assertEquals(UserState.BANNED, userBeforeUpdate.getUserState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "ACTIVE")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userModStateAfterUpdate = service.getAllUsers()
                .get(0).getUserState();
        assertEquals(UserState.ACTIVE, userModStateAfterUpdate);
    }

    @Test
    void updateUSerStateWrongState() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var user = service.getAllUsers()
                .get(0);

        final var userId = user.getId();

        assertEquals(UserState.ACTIVE, user.getUserState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "DECLINED")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
    }

    @Test
    void updateRoleTypeAdminSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var user = service.getAllUsers()
                .get(0);

        final var userId = user.getId();

        assertEquals(RoleType.USER, user.getRoleType());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "role")
                        .queryParam("type", "ADMIN")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userRoleTypeAfterUpdate = service.getAllUsers()
                .get(0).getRoleType();
        assertEquals(RoleType.ADMIN, userRoleTypeAfterUpdate);
    }

    @Test
    void updateRoleTypeWrongState() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var user = service.getAllUsers()
                .get(0);

        final var userId = user.getId();

        assertEquals(RoleType.USER, user.getRoleType());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "role")
                        .queryParam("type", "DECLINED")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
    }

    @Test
    void updateUSerStateWrongStateEmptyString() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var user = service.getAllUsers()
                .get(0);

        final var userId = user.getId();

        assertEquals(UserState.ACTIVE, user.getUserState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(106)))
                .andExpect(jsonPath("$.errorMsg", is("wrong state or role")))
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Test
    void updateRoleTypeWrongStateEmptyString() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address));

        final var user = service.getAllUsers()
                .get(0);

        final var userId = user.getId();

        assertEquals(RoleType.USER, user.getRoleType());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "role")
                        .queryParam("type", "")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(106)))
                .andExpect(jsonPath("$.errorMsg", is("wrong state or role")))
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }
}