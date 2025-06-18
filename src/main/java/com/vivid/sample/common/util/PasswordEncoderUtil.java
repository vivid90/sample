package com.vivid.sample.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderUtil {
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "abcde12#"; // 여기에 해싱할 평문 비밀번호를 입력하세요.
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // {bcrypt} 접두사 추가
        String prefixedEncodedPassword = "{bcrypt}" + encodedPassword;

        System.out.println("Raw Password: " + rawPassword);
        System.out.println("Encoded Password (BCrypt with prefix): " + prefixedEncodedPassword);
    }
}
