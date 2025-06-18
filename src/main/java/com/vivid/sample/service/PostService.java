package com.vivid.sample.service;

import com.vivid.sample.dto.PostDto;
import com.vivid.sample.dto.PostSearchParam;
import com.vivid.sample.entity.Post;
import com.vivid.sample.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    @Transactional
    public PostDto.Response createPost(PostDto.CreateRequest request, String username) {
        Post post = new Post(request.getTitle(), request.getContent(), username);
        Post savedPost = postRepository.save(post);
        return new PostDto.Response(savedPost);
    }

    @Transactional
    public PostDto.Response updatePost(Long postId, PostDto.UpdateRequest request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        Post updatedPost = postRepository.save(post);
        return new PostDto.Response(updatedPost);
    }


    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostDto.Response> searchPosts(PostSearchParam searchParam, Pageable pageable) {
        // 키워드가 비어있거나 null이면 모든 게시글 조회, 아니면 검색
        Page<Post> postPage;
        String keyword = searchParam.getKeyword();

        // 키워드가 null이거나 비어있으면 모든 게시글 조회
        if (keyword == null || keyword.trim().isEmpty()) {
            postPage = postRepository.findAll(pageable);
        } else {
            postPage = postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable);
        }
        // Page<Post>를 Page<PostDto.Response>로 변환
        return postPage.map(PostDto.Response::new);
    }

    @Transactional(readOnly = true) // 상세 조회 메소드 추가
    public PostDto.DetailResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));
        return new PostDto.DetailResponse(post);
    }
}
