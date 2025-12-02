package net.library.controller;

import net.library.model.request.BookRequest;
import net.library.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static net.library.util.HttpUtil.*;
import static net.library.util.HttpUtil.CORRELATION_ID_HEADER_NAME;
import static net.library.util.Utils.getUUID;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ActuatorControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @AfterEach
    void cleanAfter() {
        userService.deleteAll();
    }

    @Test
    void checkActuatorHealthAvailableWithoutCredentials() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/actuator/health")
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    void checkActuatorNotAvailableWithoutCredentials() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/actuator")
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void checkActuatorAvailableWithCredentials() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/actuator")
                        .with(httpBasic("user_1", PASSWORD_ADMIN))
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
