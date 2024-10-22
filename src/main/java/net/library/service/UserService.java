package net.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.library.converter.UserConverter;
import net.library.exception.UserNotFoundException;
import net.library.model.dto.UserDto;
import net.library.model.entity.User;
import net.library.model.request.UserRequest;
import net.library.model.response.UserMapper;
import net.library.repository.*;
import net.library.service.validator.UserValidationService;
import net.library.util.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.library.util.Utils.stringToLocalDateConverter;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll().stream().toList();
    }

    public Page<UserDto> getAllByFilter(String startDate, String endDate, String userName, String moderationState, String userState, String roleType, Pageable pageable) {
        log.info("Filtering with username: {}, startDate: {}, endDate: {}", userName, startDate, endDate);

        UserValidationService.isLength(userName, 3);
        var startDateConverted = stringToLocalDateConverter(startDate);
        var endDateConverted = stringToLocalDateConverter(endDate);

        ModerationState moderationStateConverted = Utils.convertToEnum(moderationState, ModerationState.class);
        UserState userStateConverted = Utils.convertToEnum(userState, UserState.class);
        RoleType roleTypeConverted = Utils.convertToEnum(roleType, RoleType.class);
        var specification = UserSpecification.filterByParam(userName, moderationStateConverted, userStateConverted, roleTypeConverted, startDateConverted, endDateConverted);

        var userPage = userRepository
                .findAll(specification, pageable);
        return userPage.map(UserMapper::toDto);
    }

    public Page<User> getAllUsersPagedSorted(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public void addUser(final UserRequest userRequest) {
        final var newUser = UserConverter.of(userRequest);
        userRepository.save(newUser);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void deleteById(UUID id) {
        int result = userRepository.deleteByUserId(id);
        if (result == 0) {
            throw new UserNotFoundException("User with ID by role type update" + id + " not found");
        }
    }

    public List<User> findAll() {
        return userRepository.findAll().stream().toList();
    }

    @Transactional
    public void updateModerationState(UUID userId, ModerationState moderationState) {
        int result = userRepository.updateModerationState(userId, moderationState);
        if (result == 0) {
            throw new UserNotFoundException("User with ID by role type update" + userId + " not found");
        }
    }

    @Transactional
    public void updateUserState(UUID userId, UserState userState) {
        int result = userRepository.updateUserState(userId, userState);
        if (result == 0) {
            throw new UserNotFoundException("User with ID by role type update" + userId + " not found");
        }
    }

    @Transactional
    public void updateRoleType(UUID userId, RoleType roleType) {
        final int result = userRepository.updateRoleType(userId, roleType);
        if (result == 0) {
            throw new UserNotFoundException("User with ID by role type update" + userId + " not found");
        }
    }
}