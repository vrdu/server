package com.example.server.controller;

import com.example.server.entity.User;
import com.example.server.rest.dto.UserGetDTO;
import com.example.server.rest.dto.UserPostDTO;
import com.example.server.rest.mapper.DTOMapper;
import com.example.server.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        User userInput = DTOMapper.INSTANCE.convertUserPostDTOToEntity(userPostDTO);
        User createdUser = userService.createUser(userInput);
        response.addCookie(getCookie(createdUser.getToken()));
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    }

    @PostMapping("/users/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO, HttpServletResponse response){
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOToEntity(userPostDTO);
        User loginUser = userService.loginUser(userInput);
        response.addCookie(getCookie(loginUser.getToken()));
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loginUser);
    }
    @GetMapping("/users/auth")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity AuthenticateUser(HttpServletRequest request){
        boolean token = userService.validateToken(request);
        if (token) {
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    public Cookie getCookie(String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);  // Prevent JavaScript access
        cookie.setSecure(true);    // Send only over HTTPS
        cookie.setPath("/");       // Make cookie accessible to entire app
        cookie.setMaxAge(60*60*10);    // 1 hour validity
        return cookie;
    }
}
