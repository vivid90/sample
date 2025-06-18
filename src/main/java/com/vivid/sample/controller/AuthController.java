package com.vivid.sample.controller;

import com.vivid.sample.dto.AuthTokens;
import com.vivid.sample.dto.LoginRequest;
import com.vivid.sample.dto.TokenResponse;
import com.vivid.sample.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

    @Tag(name = "인증", description = "인증 관련 API") // Swagger UI 그룹화
    @RestController
    @RequestMapping("/api/auth")
    @RequiredArgsConstructor
    public class AuthController {

        private final AuthService authService; // AuthenticationManager와 JwtTokenProvider 대신 AuthService 사용

        @Value("${app.security.cookie.secure:true}") // 기본값 true, HTTPS 가정
        private boolean cookieSecure;

        @Value("${app.security.cookie.same-site:Lax}") // 기본값 Lax
        private String cookieSameSite;

        @Value("${app.security.refresh-token-cookie-name:refreshToken}")
        private String refreshTokenCookieName;

        @Operation(summary = "로그인", description = "사용자 이름과 비밀번호로 로그인하고 Access Token은 응답 본문에, Refresh Token은 HttpOnly 쿠키로 발급받습니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "로그인 성공. Access Token 반환, Refresh Token 쿠키 설정",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = TokenResponse.class))),
                @ApiResponse(responseCode = "401", description = "인증 실패")
        })
        @PostMapping("/login")
        public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
            AuthTokens authTokens = authService.login(loginRequest);

            ResponseCookie refreshTokenCookie = authService.createRefreshTokenCookie(
                    authTokens.getRefreshToken(),
                    authTokens.getRefreshTokenExpiryMillis(),
                    cookieSecure,
                    cookieSameSite
            );
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            return ResponseEntity.ok(new TokenResponse(authTokens.getAccessToken()));
        }


        @Operation(summary = "토큰 재발급", description = "HttpOnly 쿠키의 Refresh Token을 사용하여 새로운 Access Token과 Refresh Token(쿠키)을 발급받습니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "토큰 재발급 성공. 새 Access Token 반환, 새 Refresh Token 쿠키 설정",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = TokenResponse.class))),
                @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token")
        })
        @Parameter(name = "refreshToken", in = ParameterIn.COOKIE, description = "Refresh Token 쿠키", required = true, schema = @Schema(type = "string")) // Swagger 문서용
        @PostMapping("/reissue")
        public ResponseEntity<TokenResponse> reissue(
                @CookieValue(name = "${app.security.refresh-token-cookie-name:refreshToken}") String refreshTokenValue, // 실제 쿠키 이름 사용
                HttpServletResponse response) {
            AuthTokens authTokens = authService.reissueToken(refreshTokenValue);

            ResponseCookie newRefreshTokenCookie = authService.createRefreshTokenCookie(
                    authTokens.getRefreshToken(),
                    authTokens.getRefreshTokenExpiryMillis(),
                    cookieSecure,
                    cookieSameSite
            );
            response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());

            return ResponseEntity.ok(new TokenResponse(authTokens.getAccessToken()));
        }

        @Operation(summary = "로그아웃", description = "서버에 저장된 사용자의 Refresh Token을 무효화하고, 클라이언트의 Refresh Token 쿠키를 삭제합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "로그아웃 성공. Refresh Token 쿠키 삭제됨."),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 인증되지 않은 사용자)")
        })
        @PostMapping("/logout")
        public ResponseEntity<Void> logout(HttpServletResponse response) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
                String userId = authentication.getName();
                authService.logout(userId); // DB에서 Refresh Token 삭제
                SecurityContextHolder.clearContext();
            }
            // 인증 여부와 관계없이 쿠키는 삭제 시도
            ResponseCookie logoutCookie = authService.createLogoutCookie(cookieSecure, cookieSameSite);
            response.addHeader(HttpHeaders.SET_COOKIE, logoutCookie.toString());

            return ResponseEntity.ok().build();
        }
    }