package net.library.service;

import net.library.model.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserServiceTest {

    @Autowired
    private UserService service;

    @Test
    void saveUser() {
        final var userRequest = new UserRequest("SADF", "Alex",
                "Bur", "a@gmail.com", "4984339834", "asfafas");

        service.addUser(userRequest);
    }
}