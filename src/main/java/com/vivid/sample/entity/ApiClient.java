package com.vivid.sample.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "api_clients") // 테이블 이름 지정
@Getter
@NoArgsConstructor // JPA를 위한 기본 생성자
public class ApiClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String serviceName; // API를 호출하는 서비스 이름

    @Column(nullable = false, unique = true)
    private String apiKey; // 고유 API 키

    @Column(nullable = false)
    private boolean enabled = true; // 키 활성화 여부

    public ApiClient(String serviceName, String apiKey) {
        this.serviceName = serviceName;
        this.apiKey = apiKey;
    }
}
