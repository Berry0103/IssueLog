package com.yuanzhang.econexus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoleDTO {
    private String rolerName; // 角色名称（如ADMIN）
    private Integer rolelevel; // 角色权限等级
}
