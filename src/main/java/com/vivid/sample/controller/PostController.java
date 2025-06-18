package com.vivid.sample.controller;

import com.vivid.sample.annotation.AuditAction;
import com.vivid.sample.dto.CommentDto;
import com.vivid.sample.dto.PostDto;
import com.vivid.sample.dto.PostSearchParam;
import com.vivid.sample.service.CommentService;
import com.vivid.sample.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시판", description = "게시글 관련 API (인증 필요)")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private  final CommentService commentService;

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 등록합니다.")
    @PostMapping
    public ResponseEntity<PostDto.Response> createPost(@Valid @RequestBody PostDto.CreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        PostDto.Response createdPost = postService.createPost(request, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @Operation(summary = "게시글 검색 및 페이징 조회",
            description = "제목 또는 내용으로 검색하고 페이징 및 정렬하여 조회합니다. " +
                    "정렬(sort) 파라미터는 'property,direction' 형식으로 사용하며, 여러 개 지정 가능합니다. " +
                    "정렬 가능한 속성: `title`, `createdAt` (예: `sort=title,asc&sort=createdAt,desc`)") // 설명 추가
    @GetMapping
    @AuditAction("게시글 검색 및 페이징 조회")
    public ResponseEntity<Page<PostDto.Response>> searchPosts(
            @ParameterObject PostSearchParam searchParam,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        // 서비스 호출 시 Pageable 객체 전달
        Page<PostDto.Response> postPage = postService.searchPosts(searchParam, pageable);
        return ResponseEntity.ok(postPage);
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 ID의 게시글 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    @AuditAction("게시글 상세 조회")
    public ResponseEntity<PostDto.DetailResponse> getPostById(
            @Parameter(name = "postId", description = "조회할 게시글의 ID", required = true, in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long postId) {
        PostDto.DetailResponse postResponse = postService.getPostById(postId);
        return ResponseEntity.ok(postResponse);
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글의 내용을 수정합니다.")
    @PutMapping("/{postId}")
    @AuditAction("게시글 수정")
    public ResponseEntity<PostDto.Response> updatePost(
            @Parameter(name = "postId", description = "수정할 게시글의 ID", required = true, in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long postId,
            @Valid @RequestBody PostDto.UpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        PostDto.Response updatedPost = postService.updatePost(postId, request, currentUsername);
        return ResponseEntity.ok(updatedPost);
    }


    @Operation(summary = "게시글 삭제", description = "기존 게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    @AuditAction("게시글 삭제")
    public ResponseEntity<Void> deletePost(
            @Parameter(name = "postId", description = "삭제할 게시글의 ID", required = true, in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        postService.deletePost(postId, currentUsername);
        return ResponseEntity.noContent().build(); // 또는 ResponseEntity.ok().build();
    }


    @Operation(summary = "게시글에 댓글 작성", description = "특정 게시글에 댓글을 작성합니다.")
    @PostMapping("/{postId}/comments")
    @AuditAction("댓글 작성") // 필요시 감사 로그 액션 추가
    public ResponseEntity<CommentDto.Response> createComment(
            @Parameter(name = "postId", description = "댓글을 작성할 게시글의 ID", required = true, in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long postId,
            @Valid @RequestBody CommentDto.CreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        CommentDto.Response createdComment = commentService.createComment(postId, request, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

}
