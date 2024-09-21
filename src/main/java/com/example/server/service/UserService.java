package com.example.server.service;


import com.example.server.entity.User;
import com.example.server.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;




@Service
@Transactional
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generate a secure key

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User loginUser(User loginUser){
        User userDatabase = userRepository.findByEmail(loginUser.getEmail());
        if (userDatabase == null){
            throw new ResponseStatusException(HttpStatus.valueOf(401), "Username is not registered");
        }
        if (!(userDatabase.getPassword().equals(loginUser.getPassword()))) {
            throw new ResponseStatusException(HttpStatus.valueOf(401), "Wrong Password");
        }
        log.debug("loged in:{}",userDatabase);
        return userDatabase;
    }
    public User createUser(User newUser){
        checkEmail(newUser.getEmail());
        checkPassword(newUser.getPassword());
        checkIfUserExists(newUser);
        String token = generateToken(newUser.getUsername());
        newUser.setToken(token);
        String username = newUser.getEmail().split("@")[0];
        newUser.setUsername(username);
        newUser = userRepository.save(newUser);
        userRepository.flush();
        log.debug("Created User:{}",newUser);
        return newUser;
    }
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10-hour expiration
                .signWith(key) // Use the key generated above
                .compact();
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
