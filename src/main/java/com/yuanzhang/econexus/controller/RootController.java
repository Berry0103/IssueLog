package com.yuanzhang.econexus.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    // 映射根路径 localhost:8080/
    @GetMapping("/")
    public String root() {
        // 获取当前用户认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 判断用户是否已登录（未登录时authentication为匿名用户）
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            // 已登录：跳转到主页index.html
            return "redirect:/index.html";
        } else {
            // 未登录：跳转到登录页login.html
            return "redirect:/login.html";
        }
    }
}
