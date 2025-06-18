package com.vivid.sample.dto;

import com.vivid.sample.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "PostCreateRequest")
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자를 넘을 수 없습니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "PostUpdateRequest")
    public static class UpdateRequest {
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자를 넘을 수 없습니다.")
        @Schema(description = "게시글 제목", example = "수정된 게시글 제목입니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        @Schema(description = "게시글 내용", example = "수정된 게시글 내용입니다.")
        private String content;
    }

    @Getter
    @NoArgsConstructor
    @Schema(name = "PostResponse") // 목록 조회용 DTO
    public static class Response {
        private Long id;
        private String title;
        private String author;
        private int commentCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Response(Post post) {
            this.id = post.getId();
            this.title = post.getTitle();
            this.author = post.getAuthor();
            this.commentCount = post.getCommentCount();
            this.createdAt = post.getCreatedAt();
            this.updatedAt = post.getUpdatedAt();
        }
    }

    @Getter
    @NoArgsConstructor
    @Schema(name = "PostDetailResponse") // 상세 조회용 DTO
    public static class DetailResponse {
        private Long id;
        private String title;
        private String content;
        private String author;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<CommentDto.Response> comments; // 댓글 목록 포함

        public DetailResponse(Post post) {
            this.id = post.getId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.author = post.getAuthor();
            this.createdAt = post.getCreatedAt();
            this.updatedAt = post.getUpdatedAt();
            if (post.getComments() != null) {
                this.comments = post.getComments().stream()
                        .map(CommentDto.Response::new)
                        .collect(Collectors.toList());
            }
        }
    }

}
