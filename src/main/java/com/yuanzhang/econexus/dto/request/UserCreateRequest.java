package com.yuanzhang.econexus.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private String firstname;
    private String lastname;

    private String departmentId; // 接收部门ID，而非实体

    private String rolerId; // 接收角色ID，而非实体

    private String userState;
}