package com.akashzorvyn.zorvynproj.dto;

import com.akashzorvyn.zorvynproj.entity.Role;
import com.akashzorvyn.zorvynproj.entity.UserStatus;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String name;
    private Role role;
    private UserStatus status;
}
