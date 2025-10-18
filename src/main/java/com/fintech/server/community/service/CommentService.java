package com.fintech.server.community.service;

import com.fintech.server.community.dto.CommentRequestDto;
import com.fintech.server.community.dto.CommentResponseDto;
import com.fintech.server.community.entity.Comment;
import com.fintech.server.community.entity.CommunityPost;
import com.fintech.server.community.repository.CommentRepository;
import com.fintech.server.community.repository.CommunityPostRepository;
import com.fintech.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    /**
     * 댓글을 작성합니다.
     */
    public CommentResponseDto createComment(Long postId, CommentRequestDto requestDto, Long userId) {
        log.info("댓글 작성 요청: postId={}, userId={}", postId, userId);

        // 사용자와 게시글 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // 댓글 생성
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(userRepository.findById(userId).orElseThrow());
        comment.setBody(requestDto.getBody());

        // 답글인 경우 부모 댓글 설정
        if (requestDto.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(requestDto.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + requestDto.getParentCommentId()));
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // 게시글의 댓글 수 업데이트
        long commentCount = commentRepository.countByPostIdAndStatus(postId, "ACTIVE");
        post.setCommentCount((int) commentCount);
        communityPostRepository.save(post);

        log.info("댓글 작성 완료: commentId={}, postId={}, userId={}", savedComment.getId(), postId, userId);

        return CommentResponseDto.from(savedComment);
    }

    /**
     * 게시글의 댓글 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        log.info("게시글 댓글 목록 조회: postId={}", postId);

        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId);
        
        return comments.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 댓글을 수정합니다.
     */
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto, Long userId) {
        log.info("댓글 수정 요청: commentId={}, userId={}", commentId, userId);

        Comment comment = commentRepository.findByIdAndStatus(commentId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // 작성자 확인
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You can only update your own comments");
        }

        comment.setBody(requestDto.getBody());
        Comment savedComment = commentRepository.save(comment);

        log.info("댓글 수정 완료: commentId={}, userId={}", commentId, userId);

        return CommentResponseDto.from(savedComment);
    }

    /**
     * 댓글을 삭제합니다 (소프트 삭제).
     */
    public void deleteComment(Long commentId, Long userId) {
        log.info("댓글 삭제 요청: commentId={}, userId={}", commentId, userId);

        Comment comment = commentRepository.findByIdAndStatus(commentId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // 작성자 확인
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own comments");
        }

        // 소프트 삭제
        comment.setStatus("DELETED");
        commentRepository.save(comment);

        // 게시글의 댓글 수 업데이트
        long commentCount = commentRepository.countByPostIdAndStatus(comment.getPost().getId(), "ACTIVE");
        comment.getPost().setCommentCount((int) commentCount);
        communityPostRepository.save(comment.getPost());

        log.info("댓글 삭제 완료: commentId={}, userId={}", commentId, userId);
    }

    /**
     * 사용자가 작성한 댓글 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByUserId(Long userId) {
        log.info("사용자 댓글 목록 조회: userId={}", userId);

        List<Comment> comments = commentRepository.findByAuthorIdAndStatusOrderByCreatedAtDesc(userId, "ACTIVE");
        
        return comments.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }
}
