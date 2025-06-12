package com.vivid.sample.controller;

import com.vivid.sample.annotation.AuditAction;
import com.vivid.sample.dto.PostDto;
import com.vivid.sample.dto.PostSearchParam;
import com.vivid.sample.entity.Post;
import com.vivid.sample.repository.PostRepository;
import com.vivid.sample.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Posts", description = "게시글 관련 API (인증 필요)")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 등록합니다.")
    @PostMapping
    public ResponseEntity<PostDto.Response> createPost(@RequestBody PostDto.CreateRequest request) {
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


}
