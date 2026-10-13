package com.example.factory.controller;

import com.example.factory.dto.RegisterRequest;
import com.example.factory.entity.User;
import com.example.factory.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanDb() {
        // Delete all users before each test to keep isolation
        userRepository.deleteAll();
    }

    @Test
    void whenRegisterValidUser_thenReturnsSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John Doe");
        request.setUsername("johndoe");
        request.setEmail("john@example.com");
        request.setPassword("securePass1");
        

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration successful! You can now log in."));

        // Verify the user was persisted with a bcrypt hash
        User saved = userRepository.findByUsername("johndoe").orElseThrow();
        assert passwordEncoder.matches("securePass1", saved.getPassword());
    }

    @Test
    void whenRegisterDuplicateUsername_thenBadRequest() throws Exception {
        // Create an existing user
        User existing = new User();
        existing.setFullName("Existing User");
        existing.setUsername("duplicate");
        existing.setEmail("dup@example.com");
        existing.setPassword(passwordEncoder.encode("pass123"));
        userRepository.save(existing);

        RegisterRequest request = new RegisterRequest();
        request.setFullName("New User");
        request.setUsername("duplicate");
        request.setEmail("new@example.com");
        request.setPassword("newpass");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username 'duplicate' is already taken."));
    }
}
