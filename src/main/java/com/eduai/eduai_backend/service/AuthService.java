package com.eduai.eduai_backend.service;

import com.eduai.eduai_backend.dto.AuthRequest;
import com.eduai.eduai_backend.dto.AuthResponse;
import com.eduai.eduai_backend.dto.RegisterRequest;
import com.eduai.eduai_backend.entity.User;
import com.eduai.eduai_backend.repository.UserRepository;
import com.eduai.eduai_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.STUDENT)
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getName(), user.getEmail(), "STUDENT");
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }
}