package com.yuanzhang.econexus.dto;

// 错误：import javax.validation.constraints.NotBlank;
// 正确：使用jakarta包
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
