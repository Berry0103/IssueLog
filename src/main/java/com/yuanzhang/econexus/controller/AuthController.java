package com.yuanzhang.econexus.controller;

import com.yuanzhang.econexus.model.User;
import com.yuanzhang.econexus.repository.UserRepository;
import com.yuanzhang.econexus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 认证控制器 - 处理登录、忘记密码、密码重置等无需登录的认证相关接口
 */
@RestController
@RequestMapping("/api/auth")  // 接口根路径保持 /api/auth
@CrossOrigin(origins = "*")   // 跨域配置（生产环境建议指定具体域名）
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    /**
     * 检查邮箱关联的用户列表
     * 请求：POST /api/auth/check-email
     * 请求体：{ "email": "user@example.com" }
     * 响应：{ "users": [{ "id": "1", "username": "testuser" }, ...] }
     */
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "邮箱不能为空"));
            }

            // 调用Service层查询该邮箱关联的用户列表
            List<User> userList = userRepository.findUsersByEmail(email);

            // 返回标准JSON格式
            return ResponseEntity.ok(Map.of("users", userList));
        } catch (Exception e) {
            e.printStackTrace();
            // 统一返回JSON错误信息（避免返回HTML）
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "查询失败：" + e.getMessage()
            ));
        }
    }

    /**
     * 发送密码重置链接到指定邮箱
     * 请求：POST /api/auth/send-reset-link
     * 请求体：{ "userId": "1", "email": "user@example.com" }
     * 响应：{ "success": true, "message": "发送成功" }
     */
    @PostMapping("/send-reset-link")
    public ResponseEntity<?> sendResetLink(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String email = request.get("email");

            if (userId == null || email == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户ID和邮箱不能为空"));
            }

            // 调用Service层发送重置链接邮件
            boolean success = userService.sendPasswordResetLink(userId, email);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "重置链接已发送至您的邮箱"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "发送失败，请稍后重试"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "发送失败：" + e.getMessage()
            ));
        }
    }

    /**
     * 重置密码确认接口
     * 请求：POST /api/auth/reset-password
     * 请求体：{ "token": "xxx", "newPassword": "xxx" }
     * 响应：{ "success": true, "message": "密码重置成功" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            // 参数校验
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "令牌不能为空"
                ));
            }

            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "密码不能为空"
                ));
            }

            // 调用Service重置密码
            boolean success = userService.resetPassword(token,newPassword);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "密码重置成功，请使用新密码登录"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "重置失败，令牌可能已过期或无效"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "重置失败：" + e.getMessage()
            ));
        }
    }
}