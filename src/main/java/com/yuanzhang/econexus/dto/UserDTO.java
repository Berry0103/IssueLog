package com.yuanzhang.econexus.dto;

import com.yuanzhang.econexus.model.Department;
import com.yuanzhang.econexus.model.Dictionary;
import com.yuanzhang.econexus.model.Role;
import com.yuanzhang.econexus.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private String userIndex;
    private String userName;
    private String email;
    private String firstname;
    private String lastname;
    private String fullName; // 计算得出的全名
    private String departmentName;
    private String departmentIndex;
    private String rolerName;
    private String userStateName;
    private String userStateIndex;
    private String language;
    private String rolerIndex;
    private Integer rolerlevel;
    private LocalDateTime lastLogin;
    private String qywxId;

    // 从实体PmUser转换为DTO
    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserIndex(user.getUserIndex());
        dto.setUserName(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setFullName(user.getFullname()); // 调用实体类的计算方法
        dto.setLanguage(user.getLanguage());
        dto.setLastLogin(user.getLastLogin());
        dto.setQywxId(user.getQywxId());

        // 处理关联对象
        Department dept = user.getDepartment();
        if (dept != null) {
            dto.setDepartmentName(dept.getDeptName());
            dto.setDepartmentIndex(dept.getDeptIndex());
        }

        Role roler = user.getRoler();
        if (roler != null) {
            dto.setRolerIndex(roler.getRolerIndex());
            dto.setRolerName(roler.getRolerName());
            dto.setRolerlevel(roler.getRolerlevel());
        }

        Dictionary userstate = user.getUserState();
        if(userstate != null){
            dto.setUserStateName(userstate.getDicName());
            dto.setUserStateIndex(userstate.getDicIndex());
        }
        return dto;
    }
}