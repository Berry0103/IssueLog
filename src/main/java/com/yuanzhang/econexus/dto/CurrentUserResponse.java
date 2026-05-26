package com.yuanzhang.econexus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CurrentUserResponse {
    private String userId;
    private String fullname; // 用户名全称
    private String username; // 登录账号（可选）
    private RoleDTO role; // 新增：用户拥有的角色列表（含等级）
}