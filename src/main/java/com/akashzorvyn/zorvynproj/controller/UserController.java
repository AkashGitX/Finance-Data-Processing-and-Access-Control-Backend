package com.akashzorvyn.zorvynproj.controller;

import com.akashzorvyn.zorvynproj.dto.*;
import com.akashzorvyn.zorvynproj.service.AuthService;
import com.akashzorvyn.zorvynproj.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    // ─── Auth ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/admin/login
     * Optional separate admin login endpoint (delegates to shared auth service).
     */
    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<LoginResponse>> adminLogin(@Valid @RequestBody LoginRequest request) {
        log.info("Admin login attempt: {}", request.getEmail());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // ─── User Management ──────────────────────────────────────────────────────

    /** POST /api/admin/users — Create a new user */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Admin creating user: {}", request.getEmail());
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    /** GET /api/admin/users — Get all users */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", userService.getAllUsers()));
    }

    /** GET /api/admin/users/{id} — Get user by ID */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", userService.getUserById(id)));
    }

    /** PUT /api/admin/users/{id} — Update user (name / role / status in one call) */
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id,
                                                           @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userService.updateUser(id, request)));
    }

    /** PUT /api/admin/users/{id}/role — Update role only */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserRole(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateUserRoleRequest request) {
        log.info("Admin updating role for user {}: {}", id, request.getRole());
        UserDTO user = userService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success("User role updated to " + request.getRole(), user));
    }

    /** PUT /api/admin/users/{id}/status — Update status only (ACTIVE / INACTIVE) */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserStatus(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdateUserStatusRequest request) {
        log.info("Admin updating status for user {}: {}", id, request.getStatus());
        UserDTO user = userService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("User status updated to " + request.getStatus(), user));
    }

    /** DELETE /api/admin/users/{id} — Delete user */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("Admin deleting user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    // ─── Legacy activate/deactivate shortcuts ─────────────────────────────────

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<UserDTO>> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User activated", userService.activateUser(id)));
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserDTO>> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User deactivated", userService.deactivateUser(id)));
    }
}
