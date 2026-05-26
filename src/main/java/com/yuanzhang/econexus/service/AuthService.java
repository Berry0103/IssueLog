package com.yuanzhang.econexus.service;
import com.yuanzhang.econexus.serviceImpl.UserDetailsServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.yuanzhang.econexus.model.User;

@Component
public class AuthService {

    private final UserDetailsServiceImpl userDetailsService;

    public AuthService(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

}