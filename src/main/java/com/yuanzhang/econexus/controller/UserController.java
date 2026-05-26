package com.yuanzhang.econexus.controller;

import com.yuanzhang.econexus.config.security.GlobalExceptionHandler;
import com.yuanzhang.econexus.dto.CurrentUserResponse;
import com.yuanzhang.econexus.dto.Issue.MesIssueDTO;
import com.yuanzhang.econexus.dto.RoleDTO;
import com.yuanzhang.econexus.dto.UserDTO;
import com.yuanzhang.econexus.model.MesIssue;
import com.yuanzhang.econexus.model.MesIssueUpdate;
import com.yuanzhang.econexus.model.User;
import com.yuanzhang.econexus.repository.UserRepository;
import com.yuanzhang.econexus.service.UserService;
import com.yuanzhang.econexus.serviceImpl.UserDetailsServiceImpl;
import com.yuanzhang.econexus.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private final UserDetailsServiceImpl userDetailsService;

    // 接口：获取当前登录用户信息（包括fullname）
    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser() {
        // 1. 从SecurityContext中获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 检查用户是否已认证
        if (authentication == null || !authentication.isAuthenticated()) {
            // 如果未认证，返回401 Unauthorized
            return ResponseEntity.status(401).build();
        }

        // 3. 获取用户名 (authentication.getPrincipal() 通常是用户名String)
        String userName = authentication.getName();

        // 4. 使用自定义方法查询用户完整信息（包含角色和部门）
        Optional<User> userOpt = userRepository.findByUserNameWithDetails(userName);

        // 5. 处理查询结果
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 6. 转换为DTO并返回
            return ResponseEntity.ok(UserDTO.fromEntity(user));
        } else {
            // 如果用户不存在（这种情况通常不应该发生），返回404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") String id) {
        User user = userService.getUserById(id);

//        return user != null ? ResponseEntity.ok(UserDTO.fromEntity(user)) : ResponseEntity.notFound().build();
        if (user != null) {
            return ResponseEntity.ok(UserDTO.fromEntity(user));
        } else {
            // 如果用户不存在（这种情况通常不应该发生），返回404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 查询所有用户列表
     * @return 用户列表
     */
    @GetMapping("/allusers")
    public ResponseEntity<List<UserDTO>> getFullUserList() {
        // 调用 Service 层方法，根据角色名查询用户
        List<User> users = userRepository.findAll();
        if (users == null){
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(users.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList()));
    }

    /**
     * 查询在职用户列表
     * @return 用户列表
     */
    @GetMapping("/userlist")
    public ResponseEntity<List<UserDTO>> getUserList() {
        // 调用 Service 层方法，根据角色名查询用户
        List<User> users = userRepository.findByUserStateDicIndexNot("dcee6111-5202-4861-a262-293a362209d5");
        if (users == null){
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(users.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList()));
    }

    // 创建
    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (!securityUtil.hasPermission(999)) {
            throw new GlobalExceptionHandler.BusinessException(403, "缺少创建用户的权限");
        }

        User createUser = userService.createUser(user);
        return ResponseEntity.ok(createUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") String id, @RequestBody User user) {
        user.setUserIndex(id);
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/update-lang")
    public ResponseEntity<User> updateUserLang(@RequestBody User user) {
        User updateuser = userService.updateUser(user);

        return ResponseEntity.ok(updateuser);
    }

    @PostMapping("/update-password")
    public ResponseEntity<User> updateUserPsd(@RequestBody User user) {
        User updateuser = userService.updateUser(user);

        return ResponseEntity.ok(updateuser);
    }

    @PostMapping("/check-email")
    public ResponseEntity<List<User>> checkEmail(@RequestBody String email) {
        // 调用 Service 层方法，根据角色名查询用户
        List<User> users = userRepository.findUsersByEmail(email);
        if (users == null){
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(users.stream()
                .collect(Collectors.toList()));
    }
}
