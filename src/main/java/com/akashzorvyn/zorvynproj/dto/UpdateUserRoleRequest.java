package com.akashzorvyn.zorvynproj.dto;

import com.akashzorvyn.zorvynproj.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}
