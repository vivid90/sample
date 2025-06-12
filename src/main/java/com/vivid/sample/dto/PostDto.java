package com.vivid.sample.dto;

import com.vivid.sample.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class PostDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateRequest {
        private String title;
        private String content;
    }

    @Getter
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String author; // 작성자 추가
        private LocalDateTime createdAt; // 등록일자 추가
        private LocalDateTime updatedAt; // 수정일자 추가

        public Response(Post post) {
            this.id = post.getId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.author = post.getAuthor(); // 작성자 매핑
            this.createdAt = post.getCreatedAt(); // 등록일자 매핑
            this.updatedAt = post.getUpdatedAt(); // 수정일자 매핑
        }
    }

}
