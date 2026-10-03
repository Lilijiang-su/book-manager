package com.bookmanager.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希工具。使用 BCrypt，密文为 60 字符，可安全存入 user.password(VARCHAR(100))。
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static String hash(String raw) {
        return ENCODER.encode(raw);
    }

    public static boolean matches(String raw, String hashed) {
        if (raw == null || hashed == null || hashed.isEmpty()) {
            return false;
        }
        return ENCODER.matches(raw, hashed);
    }
}
