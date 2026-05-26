package com.yuanzhang.econexus.config;

import com.yuanzhang.econexus.config.security.CustomAuthenticationFailureHandler;
import com.yuanzhang.econexus.config.security.CustomAuthenticationSuccessHandler;
import com.yuanzhang.econexus.serviceImpl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    // 注册自定义失败处理器
    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login.html","/reset-password.html", "/test.html", "/api/auth/*", "/css/**", "/js/**").permitAll()
                        // 根路径/需要认证（未登录时会被拦截到登录页）
                        .requestMatchers("/").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")  // 登录页面
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(customAuthenticationSuccessHandler) // 添加自定义成功处理器
                        // 使用自定义失败处理器
                        .failureHandler(customFailureHandler())
                        .permitAll()
                )
                // 配置登出
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout=true")
                        .invalidateHttpSession(true) // 清除会话
                        .deleteCookies("JSESSIONID") // 删除会话Cookie
                        .permitAll()
                )
                // 禁用默认的重定向策略对登录页的影响
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()
                        // 会话超时时间（30分钟，单位：秒）
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // 会话超时后跳转的URL（如登录页，并携带超时参数）
                        .invalidSessionUrl("/login.html?timeout=true")
                        // 可选：限制同一用户最大并发会话数（如1，表示同一账号只能在一个设备登录）
                        .maximumSessions(1)
                        // 超过最大会话数时，旧会话失效
                        .expiredUrl("/login.html?expired=true")
                );

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
