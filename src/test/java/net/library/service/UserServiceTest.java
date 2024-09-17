package net.library.service;

import net.library.model.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserServiceTest {

    @Autowired
    private final UserService service;

    @Autowired
    UserServiceTest(UserService service) {
        this.service = service;
    }

    @Test
    void saveUser() {

        final var userRequest = new UserRequest("SADF", "Alex",
                "Bur", "a@gmail.com", "4984339834", "asfafas");

        service.addUser(userRequest);
    }
}