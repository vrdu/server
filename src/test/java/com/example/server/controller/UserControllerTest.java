package com.example.server.controller;

import com.example.server.entity.User;
import com.example.server.rest.dto.UserGetDTO;
import com.example.server.rest.dto.UserPostDTO;
import com.example.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser() {
        // Arrange
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUser");
        userPostDTO.setPassword("password");

        User userEntity = new User();
        userEntity.setUsername("testUser");
        userEntity.setToken("mockToken");

        when(userService.createUser(any(User.class))).thenReturn(userEntity);

        UserGetDTO result = userController.createUser(userPostDTO, response);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(response).addCookie(any());
    }

    @Test
    void testLoginUser() {

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUser");
        userPostDTO.setPassword("password");

        User userEntity = new User();
        userEntity.setUsername("testUser");
        userEntity.setToken("mockToken");

        when(userService.loginUser(any(User.class))).thenReturn(userEntity);

        UserGetDTO result = userController.loginUser(userPostDTO, response);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(response).addCookie(any());
    }

    @Test
    void testAuthenticateUser_ValidToken() {
        when(userService.validateToken(request)).thenReturn(true);

        ResponseEntity responseEntity = userController.AuthenticateUser(request);

        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    @Test
    void testAuthenticateUser_InvalidToken() {

        when(userService.validateToken(request)).thenReturn(false);

        ResponseEntity responseEntity = userController.AuthenticateUser(request);

        assertEquals(401, responseEntity.getStatusCodeValue());
    }
}
