package com.akashzorvyn.zorvynproj.dto;

import com.akashzorvyn.zorvynproj.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
}
