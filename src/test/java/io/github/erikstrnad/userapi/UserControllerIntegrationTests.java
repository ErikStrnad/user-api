package io.github.erikstrnad.userapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.erikstrnad.userapi.model.User;
import io.github.erikstrnad.userapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController endpoints.
 * Each test method cleans the database before execution to ensure isolation.
 * This class tests the following:
 * -User registration, including successful registration and handling of duplicate usernames.
 * -User login, validating both successful login (with JWT generation) and failed login due to invalid credentials.
 * -Retrieval of the authenticated user's details using the /getUser endpoint.
 * -Retrieval of all users via the /getUsers endpoint using a valid JWT for authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        // Clean up the database before each test
        userRepository.deleteAll();
    }

    @Test
    public void testRegisterUser_Success() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User successfully registered")));
    }

    @Test
    public void testRegisterUser_ExistingUsername() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");

        // First registration
        userRepository.save(user);

        // Second registration with the same username should fail
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Username already exists")));
    }

    @Test
    public void testLoginUser_Success() throws Exception {
        // Register user first
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // Attempt login
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists());
    }

    @Test
    public void testLoginUser_InvalidCredentials() throws Exception {
        User user = new User();
        user.setUsername("nonexistent");
        user.setPassword("wrongpassword");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid username or password")));
    }

    @Test
    public void testGetUser_WithValidJWT() throws Exception {
        // Register and login to obtain a JWT token
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");

        // Register the user
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // Login to get the JWT
        String loginResponse = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String jwt = objectMapper.readTree(loginResponse).get("jwt").asText();

        // Use the token to access the /getUser endpoint
        mockMvc.perform(get("/getUser")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void testGetUsers_WithValidJWT() throws Exception {
        // Register and login a user to obtain a JWT token
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");

        // Register the user
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // Login to get the JWT token
        String loginResponse = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String jwt = objectMapper.readTree(loginResponse).get("jwt").asText();

        // Use the token to get the list of users
        mockMvc.perform(get("/getUsers")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
