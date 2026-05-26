package com.yuanzhang.econexus.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private String firstname;
    private String lastname;
    private String departmentId; // 接收部门ID
    private String rolerId; // 接收角色ID
    private String userState;
    // 注意：密码更新通常单独处理，这里暂不包含
}