package com.yuanzhang.econexus.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {
    // 密码字符集：排除易混淆字符（如 0/O、1/l/I），提升可读性
    private static final String CHAR_POOL = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz!@#$%^&*()_+-=";
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generate12BitPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            // 随机获取字符集索引
            int randomIndex = SECURE_RANDOM.nextInt(CHAR_POOL.length());
            password.append(CHAR_POOL.charAt(randomIndex));
        }
        return password.toString();
    }
}
