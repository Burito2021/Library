package net.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.library.converter.UserConverter;
import net.library.exception.NotFoundException;
import net.library.model.dto.UserDto;
import net.library.model.entity.User;
import net.library.model.mapper.UserMapper;
import net.library.model.request.UpdateUserRequest;
import net.library.model.request.UserRequest;
import net.library.repository.UserRepository;
import net.library.repository.UserSpecification;
import net.library.repository.enums.ModerationState;
import net.library.repository.enums.RoleType;
import net.library.repository.enums.UserState;
import net.library.util.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private static final int USER_MIN_FILTER_LENGTH = 3;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream().toList();
    }

    public Page<UserDto> getAllByFilter(String startDate, String endDate, String userName, String moderationState, String userState, String roleType, Pageable pageable) {
        log.info("Filtering with username: {}, startDate: {}, endDate: {}", userName, startDate, endDate);

        Utils.isLength(userName, USER_MIN_FILTER_LENGTH);
        var startDateConverted = stringToLocalDateConverter(startDate);
        var endDateConverted = stringToLocalDateConverter(endDate);

        var moderationStateConverted = Utils.convertToEnum(moderationState, ModerationState.class);
        var userStateConverted = Utils.convertToEnum(userState, UserState.class);
        var roleTypeConverted = Utils.convertToEnum(roleType, RoleType.class);
        var specification = UserSpecification.filterByParam(userName, moderationStateConverted, userStateConverted, roleTypeConverted, startDateConverted, endDateConverted);

        var userPage = userRepository
                .findAll(specification, pageable);
        return userPage.map(UserMapper::toDto);
    }

    public Page<User> getAllUsersPagedSorted(Pageable pageable) {
        return userRepository.findAll(pageable);
    }


    public UserDto addUser(final UserRequest userRequest) {

        final var newUser = UserConverter.of(userRequest, passwordEncoder);

        return UserMapper.toDto(userRepository.save(newUser));
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
            throw new NotFoundException("User with ID by role type update" + id + " not found");
        }
    }

    public List<User> findAll() {
        return userRepository.findAll().stream().toList();
    }

    @Transactional
    public void updateModerationState(UUID userId, ModerationState moderationState) {
        int result = userRepository.updateModerationState(userId, moderationState);
        if (result == 0) {
            throw new NotFoundException("User with ID by role type update" + userId + " not found");
        }
    }

    @Transactional
    public void updateUserState(UUID userId, UserState userState) {
        int result = userRepository.updateUserState(userId, userState);
        if (result == 0) {
            throw new NotFoundException("User with ID by role type update" + userId + " not found");
        }
    }

    @Transactional
    public void updateRoleType(UUID userId, RoleType roleType) {
        final int result = userRepository.updateRoleType(userId, roleType);
        if (result == 0) {
            throw new NotFoundException("User with ID by role type update" + userId + " not found");
        }
    }

    public UserDto updateUserProfile(UUID userId, UpdateUserRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getSurname() != null) user.setSurname(request.getSurname());
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        var updatedUser = userRepository.save(user);

        return UserMapper.toDto(updatedUser);
    }
}