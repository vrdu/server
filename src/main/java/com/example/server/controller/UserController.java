package com.example.server.controller;

import com.example.server.entity.User;
import com.example.server.rest.dto.UserGetDTO;
import com.example.server.rest.dto.UserPostDTO;
import com.example.server.rest.mapper.DTOMapper;
import com.example.server.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO, HttpServletResponse response){
        System.out.println("here");
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOToEntity(userPostDTO);
        User createdUser = userService.createUser(userInput);
        addTokenToCookie(response, createdUser.getToken());
        System.out.println("there");
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    }

    @PostMapping("/users/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO, HttpServletResponse response){
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOToEntity(userPostDTO);
        User loginUser = userService.loginUser(userInput);
        addTokenToCookie(response, loginUser.getToken());
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loginUser);
    }

    public void addTokenToCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);  // Prevent JavaScript access
        cookie.setSecure(true);    // Send only over HTTPS
        cookie.setPath("/");       // Make cookie accessible to entire app
        cookie.setMaxAge(3600);    // 1 hour validity
        response.addCookie(cookie);
    }
}
