package com.yuanzhang.econexus.config.security;

import jakarta.servlet.ServletException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// 自定义登录失败处理器：根据异常类型返回不同错误参数
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        // 默认错误参数
        String errorParam = "unknown_error";

        // 根据异常类型设置不同参数
        if (exception instanceof UsernameNotFoundException) {
            errorParam = "user_not_found"; // 用户不存在
        } else if (exception instanceof BadCredentialsException) {
            errorParam = "invalid_password"; // 密码错误
        } else if (exception instanceof DisabledException) {
            errorParam = "account_disabled"; // 账号被禁用
        } else if (exception instanceof LockedException) {
            errorParam = "account_locked"; // 账号被锁定
        }

        // 重定向到登录页，并携带具体错误参数
        setDefaultFailureUrl("/login.html?error=" + errorParam);
        super.onAuthenticationFailure(request, response, exception);

    }
}