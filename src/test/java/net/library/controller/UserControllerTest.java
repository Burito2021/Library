package net.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.library.config.security.SecurityConfig;
import net.library.model.entity.Book;
import net.library.model.entity.BookItem;
import net.library.model.entity.User;
import net.library.model.request.UpdateUserRequest;
import net.library.model.request.UserRequest;
import net.library.repository.UserRepository;
import net.library.repository.enums.BookItemStatus;
import net.library.repository.enums.ModerationState;
import net.library.repository.enums.RoleType;
import net.library.repository.enums.UserState;
import net.library.service.UserService;
import net.library.util.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;

import static net.library.tools.Tools.objectToStringConverter;
import static net.library.tools.Tools.threadRunner;
import static net.library.util.HttpUtil.*;
import static net.library.util.Utils.getUUID;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserService service;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void clean() {
        userRepository.deleteAll();
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    void getAllUsers() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, "pass"));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[3].username", is(username)))
                .andExpect(jsonPath("$.items[3].surname", is(surname)))
                .andExpect(jsonPath("$.items[3].name", is(name)))
                .andExpect(jsonPath("$.items[3].email", is(email)))
                .andExpect(jsonPath("$.items[3].phoneNumber", is(phoneNumber)))
                .andExpect(jsonPath("$.items[3].address", is(address)));
    }

    @Sql("classpath:sql/1_record.sql")
    @Test
    void getAllUsersOneUserInDb() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void getUsersError() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/fg")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void maxPageSizeHas100() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?size=101")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(100)));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void defaultPageSizeHas10() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(10)));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void userNameFilterLessThan3() throws Exception {
        final var xCorrelation = Utils.getUUID();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?username=ad")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelation)
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(CID, is(xCorrelation)))
                .andExpect(jsonPath(ERROR_ID, is(105)))
                .andExpect(jsonPath(ERROR_MSG, is("Filter length should be more that 2 letters")));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void userNameFilterMoreThan2() throws Exception {
        final var xCorrelation = Utils.getUUID();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?username=user99")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelation)
                        .with(httpBasic("user101", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void userNameFilterSeveralReturnValues() throws Exception {
        final var xCorrelation = Utils.getUUID();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?username=user8"+"&size=20")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelation)
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(11)));
    }

    @Sql("classpath:sql/101.sql")
    @Test
    public void userNameFilterNoValuesSpecified() throws Exception {
        final var xCorrelation = Utils.getUUID();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?username=")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelation)
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.total", is(101)))
                .andExpect(jsonPath("$.items", hasSize(10)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    public void userNamePageCheck() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?page=0&size=2")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user_3")))
                .andExpect(jsonPath("$.items[1].username", is("user_2")))
                .andExpect(jsonPath("$.items", hasSize(2)));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?page=1&size=2")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user_1")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void userNameFilterStartDate() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?start_time=2024-10-19T00:00")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user3")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void userNameFilterEndDate() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?end_time=2024-10-18T11:00")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user2")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void userNameFilterStartAndEndDate() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?start_time=2024-10-17T22:00&end_time=2024-10-18T22:00")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user1")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void userNameFilterStartDateEmptyAndSortingOrderDefaultDesc() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?start_time=&end_time=2025-10-19T23:00")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user3")))
                .andExpect(jsonPath("$.items[1].username", is("user2")))
                .andExpect(jsonPath("$.items[2].username", is("user1")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortDirectionAsc() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?order=asc")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user1")))
                .andExpect(jsonPath("$.items[1].username", is("user2")))
                .andExpect(jsonPath("$.items[2].username", is("user3")))

                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortCustomOrderDesc() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?order=desc")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username", is("user3")))
                .andExpect(jsonPath("$.items[1].username", is("user2")))
                .andExpect(jsonPath("$.items[2].username", is("user1")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortByFieldEmailOrderDefault() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?sortBy=email")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].email", is("user_3@example.com")))
                .andExpect(jsonPath("$.items[1].email", is("user_2@example.com")))
                .andExpect(jsonPath("$.items[2].email", is("user_1@example.com")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortByFieldNameOrderDefault() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?sortBy=name")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name", is("Name_3")))
                .andExpect(jsonPath("$.items[1].name", is("Name_2")))
                .andExpect(jsonPath("$.items[2].name", is("Name_1")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortByFieldSurnameCustomOrderAsc() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?sortBy=surname&order=asc")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].surname", is("Aurname")))
                .andExpect(jsonPath("$.items[1].surname", is("Burname")))
                .andExpect(jsonPath("$.items[2].surname", is("Surname")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortByFieldPhoneNumberOrderDefault() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?sortBy=phoneNumber")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].phoneNumber", is("380679920203")))
                .andExpect(jsonPath("$.items[1].phoneNumber", is("380679920202")))
                .andExpect(jsonPath("$.items[2].phoneNumber", is("380679920201")))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void sortByFieldAddressCustomOrderAsc() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "?sortBy=address&order=asc")
                        .with(httpBasic("user1", PASSWORD_ADMIN)))
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
                        .with(httpBasic("user8", PASSWORD_ADMIN))
                        .queryParam(MODERATION_STATE, "on_review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByModerationStateApprovedUpperCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user8", PASSWORD_ADMIN))
                        .queryParam(MODERATION_STATE, "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByModerationStateDeclineUpperCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user8", PASSWORD_ADMIN))
                        .queryParam(MODERATION_STATE, "Declined"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByUserStateActiveLowerCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user8", PASSWORD_ADMIN))
                        .queryParam(USER_STATE, "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(5)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByUserStateSuspendedUpperCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user8", PASSWORD_ADMIN))
                        .queryParam(USER_STATE, "SUSPENDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByUserStateBannedLowerCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user8", PASSWORD_ADMIN))
                        .queryParam(USER_STATE, "banned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByRoleTypeUserLowerCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user8", PASSWORD_ADMIN))
                        .queryParam(ROLE, "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(6)));
    }

    @Sql("classpath:sql/moderation_user_state_role.sql")
    @Test
    public void getUserByRoleTypeAdminUpperCaseExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user8", PASSWORD_ADMIN))
                        .queryParam(ROLE, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void getUserByRoleTypeAdminExistsInDb() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user1", PASSWORD_ADMIN))
                        .queryParam(ROLE, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void getUserByModerationStateEmptyValue() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user1", PASSWORD_ADMIN))
                        .queryParam(MODERATION_STATE, ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void getUserByUserStateEmptyValue() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user1", PASSWORD_ADMIN))
                        .queryParam(USER_STATE, ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/3_records.sql")
    @Test
    public void getUserByRoleTypeEmptyValue() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS)
                        .with(httpBasic("user1", PASSWORD_ADMIN))
                        .queryParam(ROLE, ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword("12345678");

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword("1212");

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isCreated())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(104)))
                .andExpect(jsonPath("$.errorMsg", is("Username already exists in Db")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(null)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isCreated());
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress("dfasf")
                .setPassword(PASSWORD_ADMIN);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isCreated());
    }

    @Sql("classpath:sql/states.sql")
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
                .setAddress(address)
                .setPassword(PASSWORD_TEST);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
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

    @Sql("classpath:sql/states.sql")
    @Test
    void findUserById() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/" + userId)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.username", is(username)))
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.surname", is(surname)))
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.phoneNumber", is(phoneNumber)))
                .andExpect(jsonPath("$.address", is(address)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void findUserByIdWhenNoUserExists() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/" + getUUID())
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isNotFound());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void findUserByNotUUID() throws Exception {
        final var xCorrelation = getUUID();
        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/" + "1212")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelation)
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", is(xCorrelation)))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void deleteById() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";

        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername).reversed())
                .skip(1).findFirst().map(User::getId).orElseThrow();

        final var userBeforeDelete = service.getAllUsers();

        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI + USERS + "/" + userId)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var user = service.getAllUsers();

        assertEquals(4, userBeforeDelete.size());
        assertEquals(3, user.size());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void deleteByIdIfNoUserExistByUUID() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI + USERS + "/" + getUUID())
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isNotFound());

        final var user = service.getAllUsers();
        assertEquals(3, user.size());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void deleteByIdIfNoUserD() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete(GLOBAL_BASE_URI + USERS + "/" + "121")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", is(notNullValue())))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
        ;
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateModerationStateUserNotFound() throws Exception {
        final var userId = getUUID();
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "moderation")
                        .queryParam("state", "approved")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateModerationStateApprovedSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var user = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        assertEquals(ModerationState.ON_REVIEW, user.getModerationState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "moderation")
                        .queryParam("state", "approved")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userModStateAfterUpdate = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null).getModerationState();
        assertEquals(ModerationState.APPROVED, userModStateAfterUpdate);
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateModerationStateDeclinedSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var user = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        assertEquals(ModerationState.ON_REVIEW, user.getModerationState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "moderation")
                        .queryParam("state", "declined")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userModStateAfterUpdate = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        assertEquals(ModerationState.DECLINED, userModStateAfterUpdate.getModerationState());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateModerationStateWrongState() throws Exception {
        final var username = "Alex";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var user = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername).reversed())
                .skip(1).findFirst().map(User::getId).orElseThrow();

        assertEquals(ModerationState.ON_REVIEW, user.getModerationState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "moderation")
                        .queryParam("state", "banned")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateUserStateUserNotFound() throws Exception {
        final var userId = getUUID();
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "ACTIVE")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateRoleTypeNotFound() throws Exception {
        final var userId = getUUID();
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "role")
                        .queryParam("type", "ADMIN")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateUserStateBannedSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();


        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var user = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        assertEquals(UserState.ACTIVE, user.getUserState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "BANNED")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userModStateAfterUpdate = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null).getUserState();

        assertEquals(UserState.BANNED, userModStateAfterUpdate);
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateUserStateSuspendedSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var user = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        assertEquals(UserState.ACTIVE, user.getUserState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "SUSPENDED")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userModStateAfterUpdate = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null).getUserState();

        assertEquals(UserState.SUSPENDED, userModStateAfterUpdate);
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateUserStateActiveSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        service.updateUserState(userId, UserState.BANNED);

        final var userBeforeUpdate = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        assertEquals(UserState.BANNED, userBeforeUpdate.getUserState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "ACTIVE")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userModStateAfterUpdate = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null).getUserState();
        assertEquals(UserState.ACTIVE, userModStateAfterUpdate);
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateUSerStateWrongState() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var user = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        assertEquals(UserState.ACTIVE, user.getUserState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "DECLINED")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateRoleTypeAdminSuccessfulUpdate() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var user = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        assertEquals(RoleType.USER, user.getRoleType());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "role")
                        .queryParam("type", "ADMIN")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isAccepted())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));

        final var userRoleTypeAfterUpdate = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null).getRoleType();
        assertEquals(RoleType.ADMIN, userRoleTypeAfterUpdate);
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateRoleTypeWrongState() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var user = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        assertEquals(RoleType.USER, user.getRoleType());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "role")
                        .queryParam("type", "DECLINED")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(110)))
                .andExpect(jsonPath("$.errorMsg", is("wrong type format")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateUSerStateWrongStateEmptyString() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var user = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        assertEquals(UserState.ACTIVE, user.getUserState());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(106)))
                .andExpect(jsonPath("$.errorMsg", is("wrong state or role")))
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateRoleTypeWrongStateEmptyString() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();
        final var user = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().orElse(null);

        assertEquals(RoleType.USER, user.getRoleType());

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "role")
                        .queryParam("type", "")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(106)))
                .andExpect(jsonPath("$.errorMsg", is("wrong state or role")))
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId));
    }

    @Test
    void appReturnsUnauthorizedWhenUserIsNotInDb() throws Exception {
        final var xCorrelationId = getUUID();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + getUUID() + "/" + "state")
                        .queryParam("state", "")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("user1", "11")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void appReturnsForbiddenWhenUserHasNoPermission() throws Exception {
        final var username = "Alelxo";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS + "/" + userId + "/" + "state")
                        .queryParam("state", "12")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .with(httpBasic("Alelxo", PASSWORD_ADMIN)))
                .andExpect(status().isForbidden());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void findUserByBasicAuthentication() throws Exception {
        final var username = "Alex";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/details")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .with(httpBasic("Alex", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.username", is(username)))
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.surname", is(surname)))
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.phoneNumber", is(phoneNumber)))
                .andExpect(jsonPath("$.address", is(address)));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void findUserByIdNotByAdminIsForbidden() throws Exception {
        final var username = "Alex";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        mvc.perform(MockMvcRequestBuilders.get(GLOBAL_BASE_URI + USERS + "/"+userId)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .with(httpBasic("user_2", PASSWORD_USER)))
                .andExpect(status().isForbidden());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateWholeProfileByIdByAdmin() throws Exception {
        final var username = "Alex";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var updatedUsername = "Alex1";
        final var updatedSurname = "Bur1";
        final var updatedName = "Alex1";
        final var updatedEmail = "efaf1@gmail.com";
        final var updatedPhoneNumber = "3806799202671";
        final var updatedAddress = "assfasfd1";
        final var updatedPassword = "User2004big";

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        final var requestBody = UpdateUserRequest.builder()
                .username(updatedUsername)
                .surname(updatedSurname)
                .name(updatedName)
                .email(updatedEmail)
                .phoneNumber(updatedPhoneNumber)
                .address(updatedAddress)
                .password(updatedPassword)
                .build();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS +"/"+ userId)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andDo(print());
        final var user = service.getUserById(userId).orElseThrow();

        assertEquals(updatedUsername, user.getUsername());
        assertEquals(updatedSurname, user.getSurname());
        assertEquals(updatedName, user.getName());
        assertEquals(updatedEmail, user.getEmail());
        assertEquals(updatedPhoneNumber, user.getPhoneNumber());
        assertEquals(updatedAddress, user.getAddress());
        assertTrue(new SecurityConfig().passwordEncoder().matches(updatedPassword, user.getPassword()));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateOneItemFromProfileByIdByAdmin() throws Exception {
        final var username = "Alex";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var updatedSurname = "Bur";

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        final var requestBody = UpdateUserRequest.builder()
                .surname(updatedSurname)
                .build();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS +"/"+ userId)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andDo(print());
        final var user = service.getUserById(userId).orElseThrow();

        assertEquals(username, user.getUsername());
        assertEquals(updatedSurname, user.getSurname());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(phoneNumber, user.getPhoneNumber());
        assertEquals(address, user.getAddress());
        assertTrue(new SecurityConfig().passwordEncoder().matches(PASSWORD_ADMIN, user.getPassword()));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateProfileByIdIsForbiddenForUser() throws Exception {
        final var username = "Alex";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var updatedUsername = "Alex1";
        final var updatedSurname = "Bur1";
        final var updatedName = "Alex1";
        final var updatedEmail = "efaf1@gmail.com1";
        final var updatedPhoneNumber = "3806799202671";
        final var updatedAddress = "assfasfd1";
        final var updatedPassword = "User2004big";

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        final var requestBody = UpdateUserRequest.builder()
                .username(updatedUsername)
                .surname(updatedSurname)
                .name(updatedName)
                .email(updatedEmail)
                .phoneNumber(updatedPhoneNumber)
                .address(updatedAddress)
                .password(updatedPassword)
                .build();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS +"/"+ userId)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_2", PASSWORD_USER)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateWholeProfileByUserEndpoint() throws Exception {
        final var xCorrelationId = getUUID();
        final var updatedUsername = "Alex1";
        final var updatedSurname = "Bur1";
        final var updatedName = "Alex1";
        final var updatedEmail = "efaf1@gmail.com";
        final var updatedPhoneNumber = "3806799202671";
        final var updatedAddress = "assfasfd1";
        final var updatedPassword = "User2004big";

        final var requestBody = UpdateUserRequest.builder()
                .username(updatedUsername)
                .surname(updatedSurname)
                .name(updatedName)
                .email(updatedEmail)
                .phoneNumber(updatedPhoneNumber)
                .address(updatedAddress)
                .password(updatedPassword)
                .build();

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername)).skip(1)
                .findFirst().map(User::getId).orElseThrow();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS +"/details")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_2", PASSWORD_USER)))
                .andExpect(status().isOk())
                .andDo(print());
        final var user = service.getUserById(userId).orElseThrow();

        assertEquals(updatedUsername, user.getUsername());
        assertEquals(updatedSurname, user.getSurname());
        assertEquals(updatedName, user.getName());
        assertEquals(updatedEmail, user.getEmail());
        assertEquals(updatedPhoneNumber, user.getPhoneNumber());
        assertEquals(updatedAddress, user.getAddress());
        assertTrue(new SecurityConfig().passwordEncoder().matches(updatedPassword, user.getPassword()));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void updateOneItemFromProfileByUserEndpoint() throws Exception {
        final var username = "Alex";
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "380679920267";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        service.addUser(new UserRequest(username, name, surname, email, phoneNumber, address, PASSWORD_ADMIN));

        final var userId = service.findAll().stream().sorted(Comparator.comparing(User::getUsername))
                .findFirst().map(User::getId).orElseThrow();

        final var updatedPhoneNumber = "3806799202671";

        final var requestBody = UpdateUserRequest.builder()
                .phoneNumber(updatedPhoneNumber)
                .build();

        mvc.perform(MockMvcRequestBuilders.patch(GLOBAL_BASE_URI + USERS +"/details")
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("Alex", PASSWORD_ADMIN)))
                .andExpect(status().isOk())
                .andDo(print());
        final var user = service.getUserById(userId).orElseThrow();

        assertEquals(username, user.getUsername());
        assertEquals(surname, user.getSurname());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(updatedPhoneNumber, user.getPhoneNumber());
        assertEquals(address, user.getAddress());
        assertTrue(new SecurityConfig().passwordEncoder().matches(PASSWORD_ADMIN, user.getPassword()));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void passwordValidatorNull() throws Exception {
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "38067992021";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(surname)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address)
                .setPassword(null);

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void passwordValidatorEmptyString() throws Exception {
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "38067992021";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(surname)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address)
                .setPassword("");

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void passwordValidatorPasswordLengthIs7NotValid() throws Exception {
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "38067992021";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(surname)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address)
                .setPassword("Sada%23");

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void passwordValidatorPasswordLengthIs8Valid() throws Exception {
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "38067992021";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(surname)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address)
                .setPassword("Sada%232");

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isCreated());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void passwordValidatorPasswordLengthIs15Valid() throws Exception {
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "38067992021";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(surname)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address)
                .setPassword("Sada%23123dfghj");

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isCreated());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void passwordValidatorPasswordLengthIs16NotValid() throws Exception {
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "38067992021";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(surname)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address)
                .setPassword("Sada%23123dfghj1");

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void passwordValidatorPasswordDoesNotHaveCapitalLetter() throws Exception {
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "38067992021";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(surname)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address)
                .setPassword("sada%231");

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void passwordValidatorPasswordDoesNotHaveSpecialSymbol() throws Exception {
        final var surname = "Bur";
        final var name = "Alex";
        final var email = "efaf@gmail.com";
        final var phoneNumber = "38067992021";
        final var address = "assfasfd";
        final var xCorrelationId = getUUID();

        final var requestBody = new User()
                .setUsername(surname)
                .setSurname(surname)
                .setName(name)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address)
                .setPassword("Sada5231");

        mvc.perform(MockMvcRequestBuilders.post(GLOBAL_BASE_URI + USERS)
                        .header(CORRELATION_ID_HEADER_NAME, xCorrelationId)
                        .contentType("application/json")
                        .content(objectToStringConverter(requestBody))
                        .with(httpBasic("user_1", PASSWORD_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(header().stringValues(CORRELATION_ID_HEADER_NAME, xCorrelationId))
                .andExpect(jsonPath("$.cid", is(xCorrelationId)))
                .andExpect(jsonPath("$.errorId", is(102)))
                .andExpect(jsonPath("$.errorMsg", is("mandatory param error")));
    }
}