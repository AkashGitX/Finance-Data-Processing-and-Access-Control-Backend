package com.akashzorvyn.zorvynproj.dto;

import com.akashzorvyn.zorvynproj.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type;
    private Long userId;
    private String name;
    private String email;
    private Role role;
}
