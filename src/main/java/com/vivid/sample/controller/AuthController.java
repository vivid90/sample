package com.vivid.sample.controller;

import com.vivid.sample.dto.LoginRequest;
import com.vivid.sample.dto.TokenResponse;
import com.vivid.sample.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Operation(summary = "로그인", description = "사용자 이름과 비밀번호를 사용하여 로그인하고 JWT 토큰을 발급받습니다.") // API 설명
    @ApiResponses(value = { // API 응답 설명
            @ApiResponse(responseCode = "200", description = "로그인 성공 및 토큰 발급",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenResponse.class))), // 성공 시 응답 DTO 명시
            @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 사용자 이름 또는 비밀번호)")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authorize(@RequestBody LoginRequest loginRequest) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        // AuthenticationManager를 사용하여 인증 수행 (내부적으로 CustomUserDetailsService 호출)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication);

        return ResponseEntity.ok(new TokenResponse(jwt));
    }

}
