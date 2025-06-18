package com.vivid.sample.dto;

import com.vivid.sample.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class CommentDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "CommentCreateRequest")
    public static class CreateRequest {
        @NotBlank(message = "댓글 내용은 필수입니다.")
        private String content;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "CommentResponse")
    public static class Response {
        private Long id;
        private String content;
        private String creator;
        private Long postId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Response(Comment comment) {
            this.id = comment.getId();
            this.content = comment.getContent();
            this.creator = comment.getCreator();
            this.createdAt = comment.getCreatedAt();
            this.updatedAt = comment.getUpdatedAt();
        }
    }
}