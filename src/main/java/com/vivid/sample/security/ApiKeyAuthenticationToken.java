package com.vivid.sample.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal; // 서비스 이름
    private final String apiKey;

    // 생성자: Principal(서비스 이름), API 키, 권한 목록을 받음
    public ApiKeyAuthenticationToken(Object principal, String apiKey, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.apiKey = apiKey;
        setAuthenticated(true); // 유효한 키로 생성되었으므로 인증된 것으로 설정
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
