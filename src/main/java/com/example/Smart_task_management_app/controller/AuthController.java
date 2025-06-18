package com.example.Smart_task_management_app.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Smart_task_management_app.entity.User;
import com.example.Smart_task_management_app.repository.UserRepository;
import com.example.Smart_task_management_app.security.JwtUtil;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "❌ Username already exists"));
        }

        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "❌ Email already exists"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role((request.getRole() != null ? request.getRole().toUpperCase() : "USER")) 
                .active(true)
                .build();

        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "✅ Signup successful!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userRepo.findByUsername(loginRequest.getUsername()).orElse(null);

            if (user == null || !user.isActive()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "❌ User not found or deactivated"));
            }

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "❌ Invalid password"));
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Login failed: " + e.getMessage()));
        }
    }

  
    @Data
    public static class LoginRequest {

        private String username;
        private String password;
    }

    @Data
    public static class SignupRequest {

        private String username;
        private String password;
        private String role;
        private String email;
    }
}
