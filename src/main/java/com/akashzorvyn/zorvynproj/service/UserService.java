package com.akashzorvyn.zorvynproj.service;

import com.akashzorvyn.zorvynproj.dto.CreateUserRequest;
import com.akashzorvyn.zorvynproj.dto.UpdateUserRequest;
import com.akashzorvyn.zorvynproj.dto.UserDTO;
import com.akashzorvyn.zorvynproj.entity.Role;
import com.akashzorvyn.zorvynproj.entity.User;
import com.akashzorvyn.zorvynproj.entity.UserStatus;
import com.akashzorvyn.zorvynproj.exception.BadRequestException;
import com.akashzorvyn.zorvynproj.exception.ResourceNotFoundException;
import com.akashzorvyn.zorvynproj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();
        User saved = userRepository.save(user);
        log.info("User created — id: {}, role: {}", saved.getId(), saved.getRole());
        return UserDTO.fromEntity(saved);
    }

    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        return UserDTO.fromEntity(findUserById(id));
    }

    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user: {}", id);
        User user = findUserById(id);
        if (request.getName() != null) user.setName(request.getName());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getStatus() != null) user.setStatus(request.getStatus());
        return UserDTO.fromEntity(userRepository.save(user));
    }

    /** PUT /api/admin/users/{id}/role */
    @Transactional
    public UserDTO updateUserRole(Long id, Role role) {
        log.info("Updating role for user {} → {}", id, role);
        User user = findUserById(id);
        user.setRole(role);
        return UserDTO.fromEntity(userRepository.save(user));
    }

    /** PUT /api/admin/users/{id}/status */
    @Transactional
    public UserDTO updateUserStatus(Long id, UserStatus status) {
        log.info("Updating status for user {} → {}", id, status);
        User user = findUserById(id);
        user.setStatus(status);
        return UserDTO.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserDTO activateUser(Long id) {
        return updateUserStatus(id, UserStatus.ACTIVE);
    }

    @Transactional
    public UserDTO deactivateUser(Long id) {
        return updateUserStatus(id, UserStatus.INACTIVE);
    }

    /** DELETE /api/admin/users/{id} */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        User user = findUserById(id);
        userRepository.delete(user);
        log.info("User deleted: {}", id);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
