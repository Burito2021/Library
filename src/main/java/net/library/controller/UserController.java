package net.library.controller;

import jakarta.annotation.PostConstruct;
import net.library.object.User;
import net.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.library.util.HttpUtil.*;

@RestController
@RequestMapping(USERS)
public class UserController {

    @Autowired
    private final UserService service;

    public UserController(final UserService service) {
        this.service = service;
    }

    @PostConstruct
    public void initUserList() {
        service.initUserLst();
    }

    @GetMapping()
    public ResponseEntity<List<User>> getUsers() {

        return new ResponseEntity<>(service.getUsers(), HttpStatus.OK);
    }
}
