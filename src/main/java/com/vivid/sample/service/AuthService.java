package com.vivid.sample.service;

import com.vivid.sample.dto.*;
import com.vivid.sample.entity.RefreshToken;
import com.vivid.sample.repository.RefreshTokenRepository;
import com.vivid.sample.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthTokens login(LoginRequest loginRequest) {
        // 1. 사용자 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String userId = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 2. 기존 Refresh Token 삭제
        // RefreshTokenRepository에 findByUserId 메서드가 정의되어 있어야 합니다.
        // 예: Optional<RefreshToken> findByUserId(String userId);
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId);
        existingToken.ifPresent(token -> {
            refreshTokenRepository.delete(token);
            refreshTokenRepository.flush();
        });


        // 3. 새로운 Access Token 및 Refresh Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(userId, authorities);
        String refreshTokenString = jwtTokenProvider.createRefreshToken(userId);
        Instant refreshTokenExpiryDate = jwtTokenProvider.getExpiryDateFromToken(refreshTokenString);
        long refreshTokenValidityMillis = jwtTokenProvider.getRefreshTokenValidityInMilliseconds();


        // 4. Refresh Token DB 저장
        RefreshToken refreshToken = RefreshToken.create(userId, refreshTokenString, refreshTokenExpiryDate);
        refreshTokenRepository.save(refreshToken);

        return new AuthTokens(accessToken, refreshTokenString, refreshTokenValidityMillis);
    }

    @Transactional
    public AuthTokens reissueToken(String requestRefreshTokenValue) {
        // 1. Refresh Token 유효성 검사
        if (!jwtTokenProvider.validateToken(requestRefreshTokenValue)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        // 2. DB에서 Refresh Token 조회 및 만료 검사
        RefreshToken refreshTokenFromDb = refreshTokenRepository.findByToken(requestRefreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh Token not found in DB"));

        // 만료된 경우 예외 처리
        if (refreshTokenFromDb.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshTokenFromDb);
            throw new RuntimeException("Expired Refresh Token (from DB check)");
        }

        // 3. 사용자 정보 조회
        String userId = jwtTokenProvider.getUserId(requestRefreshTokenValue);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // 4. 새로운 Access Token 및 Refresh Token 생성
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId);
        existingToken.ifPresent(token -> {
            refreshTokenRepository.delete(token);
            refreshTokenRepository.flush();
        });

        String newAccessToken = jwtTokenProvider.createAccessToken(userId, authorities);
        String newRefreshTokenString = jwtTokenProvider.createRefreshToken(userId);
        Instant newRefreshTokenExpiryDate = jwtTokenProvider.getExpiryDateFromToken(newRefreshTokenString);
        long newRefreshTokenValidityMillis = jwtTokenProvider.getRefreshTokenValidityInMilliseconds();

        RefreshToken newRefreshToken = RefreshToken.create(userId, newRefreshTokenString, newRefreshTokenExpiryDate);
        refreshTokenRepository.save(newRefreshToken);

        return new AuthTokens(newAccessToken, newRefreshTokenString, newRefreshTokenValidityMillis);
    }

    @Transactional
    public void logout(String userId) {
        // 로그아웃 시 해당 사용자의 Refresh Token을 DB에서 삭제
        refreshTokenRepository.deleteByUserId(userId);
    }

    public ResponseCookie createRefreshTokenCookie(String tokenValue, long maxAgeMillis, boolean secure, String sameSite) {
        return ResponseCookie.from("refreshToken", tokenValue) // 쿠키 이름은 application.properties와 일치시키거나 여기서 정의
                .httpOnly(true)
                .secure(secure) // HTTPS 환경에서 true
                .path("/api/auth") // 쿠키 사용 경로 (재발급, 로그아웃 등). 실제 API 경로에 맞게 조정
                .maxAge(maxAgeMillis / 1000) // 초 단위
                .sameSite(sameSite) // "Lax", "Strict", "None"
                .build();
    }


    public ResponseCookie createLogoutCookie(boolean secure, String sameSite) {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secure)
                .path("/api/auth") // Refresh Token 쿠키와 동일한 경로 설정
                .maxAge(0) // 쿠키 즉시 만료
                .sameSite(sameSite)
                .build();
    }

}
