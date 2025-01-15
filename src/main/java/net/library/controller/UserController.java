package net.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import net.library.model.dto.Page;
import net.library.model.dto.UserDto;
import net.library.model.request.UserRequest;
import net.library.model.response.UserMapper;
import net.library.repository.enums.ModerationState;
import net.library.repository.enums.RoleType;
import net.library.repository.enums.UserState;
import net.library.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.library.util.HttpUtil.USERS;

@RestController
@RequestMapping(USERS)
public class UserController {

    private final UserService service;

    public UserController(final UserService service) {
        this.service = service;
    }

    /**
     * retrieves a list of users on the provided filters with default sorting
     * This controller enables clients to fetch users and data of pageSize, pageNumber, totalPages, totalItems with or without filters
     * The filters that can be applied:
     * username to filter users by username
     * start_time starting timestamp to limit the range of users by createdAt param
     * end_time ending timestamp to limit the range of users by createdAt param
     * <p>
     * Pagination parameters are supported through pageable parameter allowing users
     * to retrieve specific pages and set specific size for pages to be returned
     * sorting is present by default
     *
     * @param params   a map of parameters where keys are the names of filters("start_time starting"), and the values are
     *                 values of filters ("2012-01-01T00:00")
     * @param pageable is information on page, size, sorting and default values for these params. By default, page is 0 (first),
     *                 size of the page is 10, and sorting by" username", "createdAt" in descending direction
     * @return UserDto object containing
     * pageNumber which is the number of current page
     * totalPages which is the total number of pages of users
     * totalItems which is the total number of users in the system
     * items which is the list of users after filtering, sorting and pagination
     */
    @Operation(summary = "Get all the users",
            description = "retrieve all the users with default and custom sorting by any field available in db")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "retrieved all the users successfully")
    }
    )
    @GetMapping
    public Page<UserDto> getAllUsersByFilter(
            @RequestParam Map<String, String> params,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        var sortBy = params.get("sortBy");
        var sortFields = sortBy != null && !sortBy.isEmpty()
                ? List.of(sortBy.split(","))
                : List.of("username", "createdAt");

        var direction = Sort.Direction.fromOptionalString(params.get("order")).orElse(Sort.Direction.DESC);
        var sort = Sort.by(sortFields.stream().map(field -> new Sort.Order(direction, field)).toList());

        var users = service.getAllByFilter(params.get("start_time"),
                params.get("end_time"), params.get("username"), params.get("moderation_state"), params.get("user_state"),
                params.get("role"), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));

        return new Page<>(users.getSize(), users.getNumber(), users.getTotalElements(),
                users.get().collect(Collectors.toList()));
    }

    @Operation(summary = "Add a new user",
            description = "Saved a new user successfully")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "saved a ne user successfully"),
            @ApiResponse(responseCode = "400", description = "mandatory param is missing"),
    }
    )
    @PostMapping
    public ResponseEntity<UserDto> addUser(@Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.status(201).body(service.addUser(userRequest));
    }

    @Operation(summary = "Delete all the users available in the db",
            description = "remove al the users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "removed successfully")
    }
    )
    @DeleteMapping
    public void deleteAllUsers() {
        service.deleteAll();
    }

    @Operation(summary = "Get a user by a user id",
            description = "retrieve a user by user id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "retrieved a user successfully"),
            @ApiResponse(responseCode = "404", description = "user is not found"),
            @ApiResponse(responseCode = "400", description = "when user id is not in the right format UUID"),
    }
    )
    @GetMapping("/{user_id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable(required = false, value = "user_id") final UUID userId) {
        return service.getUserById(userId)
                .map(r -> ResponseEntity.ok(UserMapper.toDto(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a specific user",
            description = "Remove a specific user by the user id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "a specific user removed successfully")
    }
    )
    @DeleteMapping("/{user_id}")
    public void deleteById(@PathVariable(required = false, value = "user_id") final UUID userId) {
        service.deleteById(userId);
    }

    @Operation(summary = "Change user's moderation status",
            description = "Change user's moderation status to  ON_REVIEW, APPROVED, DECLINED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "user's moderation status is changed successfully"),
            @ApiResponse(responseCode = "404", description = "user is not found")
    }
    )
    @PatchMapping("/{user_id}/moderation")
    public void updateModerationState(@PathVariable(required = false, value = "user_id") final UUID userId,
                                      @RequestParam(value = "state") ModerationState moderationState) {
        service.updateModerationState(userId, moderationState);
    }

    @Operation(summary = "Change user's state status",
            description = "Change user's state status to ACTIVE, BANNED, SUSPENDED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "user's state status is changed successfully"),
            @ApiResponse(responseCode = "404", description = "user is not found")
    }
    )
    @PatchMapping("/{user_id}/state")
    public void updateUserState(@PathVariable(required = false, value = "user_id") final UUID userId,
                                @RequestParam("state") UserState userState) {
        service.updateUserState(userId, userState);
    }

    @Operation(summary = "Set user's role type",
            description = "Set user's role type to  USER or ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "user's role type is set successfully"),
            @ApiResponse(responseCode = "404", description = "user is not found")
    }
    )
    @PatchMapping("/{user_id}/role")
    public void updateRoleType(@PathVariable(required = false, value = "user_id") final UUID userId,
                               @RequestParam("type") RoleType roleType) {
        service.updateRoleType(userId, roleType);
    }
}