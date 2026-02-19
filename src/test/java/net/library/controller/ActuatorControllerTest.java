package net.library.controller;

import net.library.TestContainers;
import net.library.repository.UserRepository;
import net.library.service.AuthService;
import net.library.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static net.library.tools.Tools.getAccessToken;
import static net.library.util.HttpUtil.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ActuatorControllerTest extends TestContainers {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @AfterEach
    void cleanAfter() {
        userRepository.deleteAll();
    }

    @Test
    void checkActuatorHealthAvailableWithoutCredentials() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get("/actuator/health")
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void checkActuatorNotAvailableWithoutCredentials() throws Exception {
        var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN);

        mvc.perform(MockMvcRequestBuilders.get("/actuator")
                        .header(AUTHORIZATION, BEARER + accessToken+"1")
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Sql("classpath:sql/states.sql")
    @Test
    void checkActuatorAvailableWithCredentials() throws Exception {
        var accessToken = getAccessToken(authService, ADMIN_USER, PASSWORD_ADMIN);

        mvc.perform(MockMvcRequestBuilders.get("/actuator")
                        .header(AUTHORIZATION, BEARER + accessToken)
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
