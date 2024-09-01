package net.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.library.object.User;
import net.library.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;

import static net.library.util.HttpUtil.BASE_URI;
import static net.library.util.HttpUtil.USERS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    void getUsers() throws Exception {
        var user1 = new User("Smith1", "John1", 12);
        var user2 = new User("Smith2", "John2", 22);
        var user3 = new User("Smith3", "John3", 32);

        var users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);

        mvc.perform(MockMvcRequestBuilders.get(BASE_URI + USERS))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(users)));
    }

    @Test
    void getUsersError() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(BASE_URI + USERS + "/"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.cid", notNullValue()))
                .andExpect(jsonPath("$.errorId", is(125)))
                .andExpect(jsonPath("$.errorMsg", is("global error")));
    }
}