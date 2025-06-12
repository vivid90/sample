package com.vivid.sample.service;

import com.vivid.sample.dto.PostDto;
import com.vivid.sample.dto.PostSearchParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    PostDto.Response createPost(PostDto.CreateRequest request, String username);
    Page<PostDto.Response> searchPosts(PostSearchParam searchParam, Pageable pageable); // 검색 및 페이징 조회 메소드 추가
}
