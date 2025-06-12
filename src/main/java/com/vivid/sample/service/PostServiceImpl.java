package com.vivid.sample.service;

import com.vivid.sample.dto.PostDto;
import com.vivid.sample.dto.PostSearchParam;
import com.vivid.sample.entity.Post;
import com.vivid.sample.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;

    @Transactional
    @Override
    public PostDto.Response createPost(PostDto.CreateRequest request, String username) {
        Post post = new Post(request.getTitle(), request.getContent(), username);
        Post savedPost = postRepository.save(post);
        return new PostDto.Response(savedPost);
    }

    @Transactional(readOnly = true)
    @Override
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
}
