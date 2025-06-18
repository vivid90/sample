package com.vivid.sample.service;

import com.vivid.sample.dto.CommentDto;
import com.vivid.sample.entity.Comment;
import com.vivid.sample.entity.Post;
import com.vivid.sample.entity.User;
import com.vivid.sample.repository.CommentRepository;
import com.vivid.sample.repository.PostRepository;
import com.vivid.sample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentDto.Response createComment(Long postId, CommentDto.CreateRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + username)); // 적절한 예외 처리로 변경

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + postId)); // 적절한 예외 처리로 변경

        Comment comment = Comment.builder()
                .content(request.getContent())
                .creator(username)
                .post(post)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return CommentDto.Response.builder()
                .id(savedComment.getId())
                .content(savedComment.getContent())
                .creator(savedComment.getCreator())
                .postId(savedComment.getPost().getId())
                .createdAt(savedComment.getCreatedAt())
                .updatedAt(savedComment.getUpdatedAt())
                .build();
    }
}
