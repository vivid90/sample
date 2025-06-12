package com.vivid.sample.repository;

import com.vivid.sample.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 제목 또는 내용에 키워드가 포함된 게시글 검색 (페이징 및 정렬 지원)
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword, Pageable pageable);
}
