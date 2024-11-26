package com.example.server.service;

import com.example.server.entity.User;
import com.example.server.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;
    private HttpServletRequest mockRequest;

    private User testUser;

    private User wrongTestUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        wrongTestUser = new User();
        mockRequest = mock(HttpServletRequest.class);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(userRepository);
    }

    @Test
    void testLoginUser_Successful() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(testUser);

        User result = userService.loginUser(testUser);

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findByEmail(testUser.getEmail());
    }
    @Test
    void testLoginUser_EmailNotRegistered() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> userService.loginUser(testUser));
    }

    @Test
    void testLoginUser_WrongPassword() {
        wrongTestUser.setPassword("wrongpassword");
        wrongTestUser.setEmail("test@example.com");
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(testUser);

        assertThrows(ResponseStatusException.class, () -> userService.loginUser(wrongTestUser));
    }


    @Test
    void testGenerateToken() {
        String token = userService.generateToken(testUser.getEmail());

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }



    @Test
    void testValidateToken_ValidToken() {
        // Mocking a valid token
        String validToken = "valid-token";
        Cookie tokenCookie = new Cookie("token", validToken);
        when(mockRequest.getCookies()).thenReturn(new Cookie[]{tokenCookie});
        when(userRepository.findByToken(validToken)).thenReturn(new User());

        // Act and Assert
        boolean result = userService.validateToken(mockRequest);
        assertTrue(result, "Expected the token to be valid");
        verify(userRepository).findByToken(validToken);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Mocking an invalid token
        String invalidToken = "invalid-token";
        Cookie tokenCookie = new Cookie("token", invalidToken);
        when(mockRequest.getCookies()).thenReturn(new Cookie[]{tokenCookie});
        when(userRepository.findByToken(invalidToken)).thenReturn(null);

        // Act and Assert
        boolean result = userService.validateToken(mockRequest);
        assertFalse(result, "Expected the token to be invalid");
        verify(userRepository).findByToken(invalidToken);
    }

    @Test
    void testValidateToken_NoTokenProvided() {
        // Mocking no token in cookies
        when(mockRequest.getCookies()).thenReturn(null);

        // Act and Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.validateToken(mockRequest),
                "Expected a ResponseStatusException to be thrown for missing token"
        );
        assertEquals("No token was sent, please try to login.", exception.getReason());
        verify(userRepository, never()).findByToken(anyString());
    }
    @Test
    void testCreateUser_Successful() {

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUser(testUser);

        // Assertions
        assertNotNull(createdUser, "Created user should not be null");
        assertEquals("test", createdUser.getUsername(), "Username should be set correctly");
        assertNotNull(createdUser.getToken(), "Token should be generated");
        verify(userRepository).save(testUser);
        verify(userRepository).flush();
    }
    @Test
    void testCreateUser_checkEmail_noAt(){
        testUser.setEmail("testexample.com");
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));

    }
    @Test
    void testCreateUser_checkEmail_space(){
        testUser.setEmail("test @example.com");
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }
    @Test
    void testCreateUser_checkEmail_noDot(){
        testUser.setEmail("test@examplecom");
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }
    @Test
    void testCreateUser_checkPassword_space(){
        testUser.setPassword("12 312341234");
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }
    @Test
    void testCreateUser_checkPassword_short(){
        testUser.setPassword("123");
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }
    @Test
    void testCreateUser_CheckIfUserExists_UserAlreadyExists() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(testUser);

        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }


}


