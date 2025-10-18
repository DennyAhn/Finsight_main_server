package com.fintech.server.community.service;

import com.fintech.server.community.dto.LikeResponseDto;
import com.fintech.server.community.entity.CommunityPost;
import com.fintech.server.community.entity.PostLike;
import com.fintech.server.community.repository.CommunityPostRepository;
import com.fintech.server.community.repository.PostLikeRepository;
import com.fintech.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    /**
     * 게시글에 좋아요를 토글합니다 (좋아요/취소).
     */
    public LikeResponseDto toggleLike(Long postId, Long userId) {
        log.info("게시글 좋아요 토글 요청: postId={}, userId={}", postId, userId);

        // 사용자와 게시글 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // 현재 좋아요 상태 확인
        boolean isLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        
        if (isLiked) {
            // 좋아요 취소
            postLikeRepository.deleteByUserIdAndPostId(userId, postId);
            post.setLikeCount(post.getLikeCount() - 1);
            communityPostRepository.save(post);
            
            log.info("게시글 좋아요 취소: postId={}, userId={}", postId, userId);
            
            return LikeResponseDto.builder()
                    .liked(false)
                    .likeCount(post.getLikeCount())
                    .message("좋아요를 취소했습니다.")
                    .build();
        } else {
            // 좋아요 추가
            PostLike postLike = new PostLike();
            postLike.setUserId(userId);
            postLike.setPostId(postId);
            postLikeRepository.save(postLike);
            
            post.setLikeCount(post.getLikeCount() + 1);
            communityPostRepository.save(post);
            
            log.info("게시글 좋아요 추가: postId={}, userId={}", postId, userId);
            
            return LikeResponseDto.builder()
                    .liked(true)
                    .likeCount(post.getLikeCount())
                    .message("좋아요를 눌렀습니다.")
                    .build();
        }
    }

    /**
     * 사용자가 특정 게시글에 좋아요를 눌렀는지 확인합니다.
     */
    public boolean isLikedByUser(Long postId, Long userId) {
        return postLikeRepository.existsByUserIdAndPostId(userId, postId);
    }

    /**
     * 게시글의 좋아요 수를 조회합니다.
     */
    public long getLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    /**
     * 사용자가 좋아요한 게시글 ID 목록을 조회합니다.
     */
    public java.util.List<Long> getLikedPostIds(Long userId) {
        return postLikeRepository.findPostIdsByUserId(userId);
    }
}
