package com.vivid.sample.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "comments") // 테이블명은 원하는 대로 지정 가능
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob // 긴 텍스트를 위해 @Lob 어노테이션 사용 가능
    @Column(nullable = false)
    private String content;

    private String creator; // 댓글 작성자 username

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id", nullable = false) // Post 엔티티의 id 컬럼을 참조
    private Post post;
}