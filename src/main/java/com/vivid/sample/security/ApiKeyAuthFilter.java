package com.vivid.sample.security;

import com.vivid.sample.entity.ApiClient;
import com.vivid.sample.repository.ApiClientRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor // final 필드 생성자 자동 생성
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final String headerName;
    private final ApiClientRepository apiClientRepository; // Map 대신 Repository 주입


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String apiKeyFromHeader = request.getHeader(headerName);

        if (apiKeyFromHeader != null) {
            // DB에서 활성화된 API 키 조회
            Optional<ApiClient> clientOptional = apiClientRepository.findByApiKeyAndEnabledTrue(apiKeyFromHeader);

            if (clientOptional.isPresent()) {
                ApiClient client = clientOptional.get();
                // 서비스 이름을 Principal로 사용하여 인증 토큰 생성
                ApiKeyAuthenticationToken authentication = new ApiKeyAuthenticationToken(
                        client.getServiceName(), // DB에서 조회한 서비스 이름 사용
                        apiKeyFromHeader,
                        AuthorityUtils.createAuthorityList("ROLE_SERVICE")
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
