package com.example.server.service;


import com.example.server.entity.User;
import com.example.server.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;



@Service
@Transactional
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User newUser){
        checkEmail(newUser.getEmail());
        checkPassword(newUser.getPassword());
        checkIfUserExists(newUser);
        newUser.setToken(UUID.randomUUID().toString());
        String username = newUser.getEmail().split("@")[0];
        newUser.setUsername(username);
        newUser = userRepository.save(newUser);
        userRepository.flush();
        log.debug("Created User:{}",newUser);
        return newUser;
    }

    private void checkEmail(String email){
        if (!email.contains("@") || email.contains(" ") || !email.contains(".")) {
            throw new ResponseStatusException(HttpStatus.valueOf(404), "Email not valid");
        }
    }

    private void checkPassword(String password) {
        if (password.contains(" ")) {
            throw new ResponseStatusException(HttpStatus.valueOf(404), "Password can't contain space");
        }
        if (password.length() < 5) {
            throw new ResponseStatusException(HttpStatus.valueOf(404), "Password must be at least 5 characters");
        }
    }

    private void checkIfUserExists(User userToBeCreated){
        User userByEmail = userRepository.findByEmail(userToBeCreated.getEmail());
        if (userByEmail != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "add User failed because email is already used");
        }
    }



}
