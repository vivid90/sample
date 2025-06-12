package com.vivid.sample.util;

import java.security.SecureRandom;
import java.util.Base64;

public class ApiKeyGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding(); // URL-safe, padding 없음

    // 기본 키 길이 (바이트 단위, Base64 인코딩 후 길이는 약 4/3배가 됨)
    private static final int DEFAULT_KEY_LENGTH_BYTES = 32; // 예: 32바이트 -> 약 43자 Base64 문자열

    /**
     * 지정된 길이(바이트)의 안전한 랜덤 API 키를 생성합니다.
     * @param lengthBytes 생성할 키의 바이트 길이
     * @return Base64 URL-safe 인코딩된 API 키 문자열
     */
    public static String generateApiKey(int lengthBytes) {
        if (lengthBytes <= 0) {
            throw new IllegalArgumentException("키 길이는 0보다 커야 합니다.");
        }
        byte[] randomBytes = new byte[lengthBytes];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    /**
     * 기본 길이(32바이트)의 안전한 랜덤 API 키를 생성합니다.
     * @return Base64 URL-safe 인코딩된 API 키 문자열
     */
    public static String generateApiKey() {
        return generateApiKey(DEFAULT_KEY_LENGTH_BYTES);
    }

    // 메인 메소드 (테스트용)
    public static void main(String[] args) {
        String newApiKey = generateApiKey();
        System.out.println("Generated API Key: " + newApiKey);
        System.out.println("Key Length (chars): " + newApiKey.length());

        String shortApiKey = generateApiKey(16); // 16 바이트 키 생성
        System.out.println("Generated Short API Key: " + shortApiKey);
        System.out.println("Short Key Length (chars): " + shortApiKey.length());
    }
}
