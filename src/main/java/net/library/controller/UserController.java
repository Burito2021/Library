package net.library.controller;

import jakarta.validation.Valid;
import net.library.model.dto.UserDto;
import net.library.model.request.UserRequest;
import net.library.model.response.UserMapper;
import net.library.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static net.library.util.HttpUtil.USERS;

@RestController
@RequestMapping(USERS)
public class UserController {

    private final UserService service;

    public UserController(final UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return UserMapper.toDto(service.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<Void> addUser(@Valid @RequestBody UserRequest userRequest) {
        service.addUser(userRequest);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping
    public void deleteAllUsers() {
        service.deleteAll();
    }

    @GetMapping("/{user_id}")
    public UserDto getUserById(@PathVariable(required = false, value = "user_id") final UUID userId) {
        return UserMapper.toDto(service.getUserById(userId));
    }

    @DeleteMapping("/{user_id}")
    public void deleteById(@PathVariable(required = false, value = "user_id") final UUID userId) {
        service.deleteById(userId);
    }
}
   