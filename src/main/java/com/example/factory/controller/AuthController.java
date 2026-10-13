package com.example.factory.controller;

import com.example.factory.dto.AuthRequest;
import com.example.factory.dto.AuthResponse;
import com.example.factory.dto.RegisterRequest;
import com.example.factory.entity.User;
import com.example.factory.repository.UserRepository;
import com.example.factory.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // The identifier can be either a username or an email.
            // CustomUserDetailsService handles the lookup by username-or-email.
            String identifier = request.getIdentifier();

            // Resolve the actual username for authentication
            // If the identifier is an email, find the user and get their username
            String usernameForAuth = identifier;
            if (identifier != null && identifier.contains("@")) {
                User userByEmail = userRepository.findByEmail(identifier).orElse(null);
                if (userByEmail != null) {
                    usernameForAuth = userByEmail.getUsername();
                }
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameForAuth, request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Find the user to build the response
            User user = userRepository.findByUsername(usernameForAuth)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String jwt = jwtUtil.generateToken(user.getUsername());

            return ResponseEntity.ok(new AuthResponse(
                    jwt,
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole()
            ));
        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username/email or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "An error occurred during login. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        Map<String, String> response = new HashMap<>();

        // Validate required fields
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            response.put("error", "Username is required.");
            return ResponseEntity.badRequest().body(response);
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            response.put("error", "Email is required.");
            return ResponseEntity.badRequest().body(response);
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            response.put("error", "Password must be at least 6 characters.");
            return ResponseEntity.badRequest().body(response);
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            response.put("error", "Full name is required.");
            return ResponseEntity.badRequest().body(response);
        }

        // Check for duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            response.put("error", "Username '" + request.getUsername() + "' is already taken.");
            return ResponseEntity.badRequest().body(response);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            response.put("error", "Email '" + request.getEmail() + "' is already registered.");
            return ResponseEntity.badRequest().body(response);
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        response.put("message", "Registration successful! You can now log in.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, String> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());
        return ResponseEntity.ok(response);
    }
}
