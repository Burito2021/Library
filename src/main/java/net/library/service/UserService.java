package net.library.service;

import net.library.object.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final List<User> users = new ArrayList<>();

    public void initUserLst() {
        users.add(new User("Smith1", "John1", 12));
        users.add(new User("Smith2", "John2", 22));
        users.add(new User("Smith3", "John3", 32));
    }

    public List<User> getUsers() {
        LOGGER.debug("List of books returned: {} ",users);
        return users;
    }

    public void deleteAllUsers(){

        users.clear();
        LOGGER.info("All the users deleted");
    }

    public void addUser(User user){

        users.add(user);
        LOGGER.info("Such user added: {}", user);
    }
}
