package net.library.controller;

import jakarta.validation.Valid;
import net.library.exception.MdcUtils;
import net.library.model.dto.UserDto;
import net.library.model.request.UserRequest;
import net.library.model.response.ApiResponse;
import net.library.model.response.LibraryResponse;
import net.library.model.response.UserMapper;
import net.library.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static net.library.service.validator.UserValidationService.msisdnCheckFormat380;
import static net.library.service.validator.UserValidationService.usernameValidator;
import static net.library.util.Constants.CORRELATION_ID_HEADER_NAME;
import static net.library.util.HttpUtil.USERS;
import static net.library.util.Utils.deleteSpacesHyphens;

@RestController
@RequestMapping(USERS)
public class UserController {

    private final UserService service;

    public UserController(final UserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {

        return ResponseEntity.ok()
                .header(CORRELATION_ID_HEADER_NAME, MdcUtils.getCid())
                .body(new ApiResponse<>(UserMapper.toDto(service.getAllUsers())));
    }

    @PostMapping
    public ResponseEntity<LibraryResponse> addUser(@Valid @RequestBody UserRequest userRequest) {
        final var refinedMsisdn = deleteSpacesHyphens(userRequest.getPhoneNumber());

        msisdnCheckFormat380(refinedMsisdn);
        usernameValidator(UserMapper.toDto(service.getAllUsers()), userRequest.getUsername());

        service.addUser(userRequest);
        return ResponseEntity.ok(LibraryResponse.of(MdcUtils.getCid()));
    }

    @DeleteMapping
    public ResponseEntity<LibraryResponse> deleteAllUsers() {

        service.deleteAll();
        return ResponseEntity.ok((LibraryResponse.of(MdcUtils.getCid())));
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable(required = false, value = "user_id") final UUID userId) {

        return ResponseEntity.ok()
                .header(CORRELATION_ID_HEADER_NAME, MdcUtils.getCid())
                .body(new ApiResponse<>(UserMapper.toDto(service.getUserById(userId))));
    }

    @DeleteMapping("/{user_id}")
    public ResponseEntity<LibraryResponse> deleteById(@PathVariable(required = false, value = "user_id") final UUID userId) {
        service.deleteById(userId);

        return new ResponseEntity<>(LibraryResponse.of(MdcUtils.getCid()), HttpStatus.OK);
    }
}
