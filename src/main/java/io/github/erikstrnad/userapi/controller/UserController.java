package io.github.erikstrnad.userapi.controller;

import io.github.erikstrnad.userapi.dto.AuthenticationResponse;
import io.github.erikstrnad.userapi.dto.UserResponse;
import io.github.erikstrnad.userapi.model.User;
import io.github.erikstrnad.userapi.repository.UserRepository;
import io.github.erikstrnad.userapi.service.CustomUserDetailsService;
import io.github.erikstrnad.userapi.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserController is a REST controller responsible for managing user operations.
 * It leverages Spring Security's AuthenticationManager for authentication,
 * a CustomUserDetailsService to load user details, and JwtUtil to handle JWT token generation and validation.
 * Passwords are securely hashed using a PasswordEncoder (BCrypt).
 */
@RestController
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor for UserController.
     *
     * @param authenticationManager used to authenticate user credentials.
     * @param userRepository used for CRUD operations on User entities.
     * @param userDetailsService service to load user details.
     * @param jwtUtil utility for JWT token operations.
     * @param passwordEncoder encoder for hashing passwords.
     */
    public UserController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * POST /register
     * Registers a new user.
     *
     * @param user The user data provided in the request body.
     * @return A ResponseEntity with a message indicating success or failure.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Username already exists");
        }
        // Encode (hash) the user's password before saving.
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User successfully registered");
    }

    /**
     * POST /login
     * Authenticates a user and generates a JWT token.
     *
     * @param loginRequest The login credentials provided in the request body.
     * @return A ResponseEntity containing an AuthenticationResponse with the JWT token if successful, or an error message.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        String jwt = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

    /**
     * GET /getUser
     * Returns data for the currently authenticated user.
     *
     * @param authentication The authentication object representing the current user.
     * @return A ResponseEntity containing the user's details if found, or an error message.
     */
    @GetMapping("/getUser")
    public ResponseEntity<?> getUser(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Create a DTO without sensitive data like the password.
            UserResponse response = new UserResponse(user.getId(), user.getUsername());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    /**
     * GET /getUsers
     * Returns a list of all users.
     *
     * @return A ResponseEntity containing a list of UserResponse DTOs.
     */
    @GetMapping("/getUsers")
    public ResponseEntity<?> getUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(user -> new UserResponse(user.getId(), user.getUsername()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
