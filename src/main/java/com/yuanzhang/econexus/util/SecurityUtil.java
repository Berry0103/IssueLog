package com.yuanzhang.econexus.util;

import com.yuanzhang.econexus.config.security.GlobalExceptionHandler;
import com.yuanzhang.econexus.dto.CurrentUserResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    /**
     * 获取当前登录用户的权限信息
     */
    public CurrentUserResponse getCurrentLoginUser() {
        // 1. 从Security上下文获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new GlobalExceptionHandler.NotLoginException("未登录，无法执行操作");
        }

        // 2. 解析用户信息（需自定义UserDetails实现类，封装权限）
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            // 假设自定义UserDetails实现类为CustomUserDetails，包含权限集合
            CurrentUserResponse userDetails = (CurrentUserResponse) principal;
            return userDetails;
        } else {
            throw new RuntimeException("用户信息解析失败");
        }
    }

    /**
     * 校验当前用户是否拥有指定权限
     */
    public boolean hasPermission(Integer permission) {
        CurrentUserResponse loginUser = getCurrentLoginUser();
        return loginUser.getRole() != null && (loginUser.getRole().getRolelevel() > permission);
    }

}