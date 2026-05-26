package com.yuanzhang.econexus.config.security;

import com.yuanzhang.econexus.repository.UserRepository;
import com.yuanzhang.econexus.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 自定义登录成功处理器 - 用于更新用户最后登录时间
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 1. 获取登录成功的用户信息
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername(); // 假设用户名是唯一标识

        // 2. 更新用户表的lastLogin字段（替换为你的实际业务逻辑）
        userRepository.updateLastLoginByUserIndex(LocalDateTime.now(),username);

        // 3. 登录成功后的跳转/响应处理（根据你的业务需求调整）
        // 方式1：前后端分离 - 返回JSON响应
//        response.setContentType("application/json;charset=UTF-8");
//        response.getWriter().write("{\"code\":200,\"msg\":\"登录成功\",\"data\":null}");

        // 方式2：前后端不分离 - 重定向到首页（根据你的需求选择）
         response.sendRedirect("/main");
    }
}