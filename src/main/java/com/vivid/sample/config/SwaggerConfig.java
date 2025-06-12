package com.vivid.sample.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "dSample API",
                description = "Sample API Documentation",
                version = "v1.0.0"))
@Configuration
public class SwaggerConfig {

    // application.yml 또는 application.properties 에서 API 키 헤더 이름 주입
    @Value("${app.security.api-key-header:X-API-Key}") // 기본값으로 X-API-Key 사용
    private String apiKeyHeaderName;

    @Bean
    public OpenAPI openAPI() {
        // 1. API 키 Security 스키마 정의
        SecurityScheme apiKeyAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(apiKeyHeaderName);

        // 2. Bearer 인증 Security 스키마 정의 (정의는 유지)
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 3. Security 요청 정의 (apiKeyAuth와 bearerAuth 모두 포함하도록 복원)
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("apiKeyAuth")
                .addList("bearerAuth");

        return new OpenAPI()
                // 4. Components 설정에 두 가지 SecurityScheme 모두 추가 (Authorize 팝업에는 둘 다 표시됨)
                .components(new Components()
                        .addSecuritySchemes("apiKeyAuth", apiKeyAuth)
                        .addSecuritySchemes("bearerAuth", bearerAuth)
                )
                // 5. API 전체에 apiKeyAuth SecurityRequirement만 적용
                .addSecurityItem(securityRequirement);
    }
}