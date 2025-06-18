package com.vivid.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthTokens {
    private String accessToken;
    private String refreshToken;
    private Long refreshTokenExpiryMillis; // 쿠키 Max-Age 설정을 위함
}
