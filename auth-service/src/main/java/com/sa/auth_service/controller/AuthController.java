package com.sa.auth_service.controller;

import com.sa.auth_service.dto.LoginRequest;
import com.sa.auth_service.dto.RegisterRequest;
import com.sa.auth_service.entity.User;
import com.sa.auth_service.exception.EmailAlreadyRegisteredException;
import com.sa.auth_service.exception.InvalidCredentialsException;
import com.sa.auth_service.repository.UserRepository;
import com.sa.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping("/test")
    public String test(){
        return "this is a test route";
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        try {
            String message = authService.register(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(message);

        } catch (EmailAlreadyRegisteredException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }


    @PostMapping("/jwtlogin")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {

        try {
            String token = authService.login(request);

            return ResponseEntity.ok(token);

        } catch (InvalidCredentialsException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }


    @GetMapping("/getprofile")
    public ResponseEntity<User> getProfile(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }
}