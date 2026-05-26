package com.yuanzhang.econexus.dto;

import com.yuanzhang.econexus.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String message;
    private int errorCode;
    private String username;
    private Set<String> roles;
}
    