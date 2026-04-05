package com.akashzorvyn.zorvynproj.service;

import com.akashzorvyn.zorvynproj.dto.LoginRequest;
import com.akashzorvyn.zorvynproj.dto.LoginResponse;
import com.akashzorvyn.zorvynproj.entity.User;
import com.akashzorvyn.zorvynproj.entity.UserStatus;
import com.akashzorvyn.zorvynproj.exception.UnauthorizedException;
import com.akashzorvyn.zorvynproj.repository.UserRepository;
import com.akashzorvyn.zorvynproj.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            log.warn("Login blocked for inactive user: {}", request.getEmail());
            throw new UnauthorizedException("Account is inactive. Please contact the administrator");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        log.info("Login successful for user: {} with role: {}", user.getEmail(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
