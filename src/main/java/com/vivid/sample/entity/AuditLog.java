package com.vivid.sample.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username; // 요청 사용자 또는 서비스 이름

    @Column(nullable = false)
    private String requestUri; // 요청 URI

    @Column(nullable = false)
    private String httpMethod; // HTTP 메소드

    @Lob // 파라미터가 길 수 있으므로 LOB 타입 사용 고려
    @Column(columnDefinition = "TEXT") // 또는 "LONGTEXT" 등 필요에 따라
    private String parameters; // 요청 파라미터 (필터링된)

    @Column
    private Integer statusCode; // 응답 상태 코드 (선택적)

    @Column // 행위 설명 필드 추가
    private String action;

    @Builder
    public AuditLog(String username, String requestUri, String httpMethod, String parameters, Integer statusCode, String action) {
        this.username = username;
        this.requestUri = requestUri;
        this.httpMethod = httpMethod;
        this.parameters = parameters;
        this.statusCode = statusCode;
        this.action = action;
    }
}
