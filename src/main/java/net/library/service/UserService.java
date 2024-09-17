package net.library.service;

import net.library.model.entity.User;
import net.library.model.request.UserConverter;
import net.library.model.request.UserRequest;
import net.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {

        return StreamSupport.stream(userRepository.findAll().spliterator(), false).toList();
    }

    public void addUser(final UserRequest userRequest) {

        final var newUser = UserConverter.of(userRequest);
        userRepository.save(newUser);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public User getUserById(UUID id) {
        var user = userRepository.findById(id).orElse(null);
        return user;
    }

    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }
}
