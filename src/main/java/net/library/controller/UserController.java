package net.library.controller;

import jakarta.validation.Valid;
import net.library.exception.MdcUtils;
import net.library.model.request.UserRequest;
import net.library.model.response.LibraryResponse;
import net.library.model.entity.User;
import net.library.model.response.UserResponse;
import net.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.library.util.HttpUtil.USERS;

@RestController
@RequestMapping(USERS)
public class UserController {

    @Autowired
    private final UserService service;

    public UserController(final UserService service) {
        this.service = service;
    }

    @GetMapping()
    public ResponseEntity<UserResponse> getAllUsers() {

        return new ResponseEntity<>(new UserResponse(MdcUtils.getCid(),service.getAllUsers()),HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<LibraryResponse> addUser(@Valid @RequestBody UserRequest userRequest) {

         service.addUser(userRequest);
        return ResponseEntity.ok(LibraryResponse.of(MdcUtils.getCid()));
    }

    @DeleteMapping()
    public ResponseEntity<LibraryResponse>deleteAllUsers(){

        service.deleteAll();
        return ResponseEntity.ok((LibraryResponse.of(MdcUtils.getCid())));
    }
}
