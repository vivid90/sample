package com.vivid.sample.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "posts") // 테이블 이름 지정 (선택 사항)
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob // 긴 텍스트를 위한 어노테이션
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String author; // 작성자 필드 추가

    // 생성자 수정
    public Post(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }
}
