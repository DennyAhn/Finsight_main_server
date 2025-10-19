package com.fintech.server.community.controller;

import com.fintech.server.community.dto.*;
import com.fintech.server.community.service.CommunityService;
import com.fintech.server.community.service.PostLikeService;
import com.fintech.server.community.service.CommentService;
import com.fintech.server.service.AuthService;
import com.fintech.server.dto.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/community/posts") // context-path(/api) 제외
@RequiredArgsConstructor
@Slf4j
public class CommunityController {

    private final CommunityService communityService;
    private final PostLikeService postLikeService;
    private final CommentService commentService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@RequestBody PostRequestDto requestDto, 
                                                     HttpServletRequest request) {
        try {
            // Spring Security Context에서 인증된 사용자 ID 가져오기
            Long currentUserId = getCurrentUserId(request);
            System.out.println("Current User ID: " + currentUserId);

            PostResponseDto responseDto = communityService.createPost(requestDto, currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 현재 인증된 사용자 ID를 가져오는 메서드
     * JWT 토큰이 있으면 토큰에서 추출, 없으면 기존 게스트 사용자 ID 사용
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            // 1. Spring Security Context에서 인증된 사용자 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof Long) {
                Long authenticatedUserId = (Long) authentication.getPrincipal();
                return authenticatedUserId;
            }
            
            // 2. Authorization 헤더에서 JWT 토큰 확인
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authService.validateToken(authHeader)) {
                return authService.getUserIdFromToken(authHeader);
            }
            
        } catch (Exception e) {
            // JWT 토큰 처리 실패시 기존 게스트 사용자 ID 사용
        }
        
        // 3. 토큰이 없거나 유효하지 않은 경우 게스트 사용자 생성 후 ID 반환
        try {
            TokenResponseDto guestResponse = authService.createGuestUserAndLogin();
            log.info("자동 게스트 사용자 생성: userId={}", guestResponse.getUserId());
            return guestResponse.getUserId();
        } catch (Exception e) {
            log.error("게스트 사용자 자동 생성 실패, 기본값 사용", e);
            return 1330L; // 최후의 수단
        }
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts(HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            List<PostResponseDto> posts = communityService.findAllPosts(currentUserId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            // 사용자 인증 실패 시 기본 목록 반환
            List<PostResponseDto> posts = communityService.findAllPosts();
            return ResponseEntity.ok(posts);
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId, HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            PostResponseDto post = communityService.findPostById(postId, currentUserId);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(@PathVariable Long postId,
                                                    @RequestBody PostRequestDto requestDto,
                                                    HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            PostResponseDto response = communityService.updatePost(postId, requestDto, currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            communityService.deletePost(postId, currentUserId);
            return ResponseEntity.ok("포스트가 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ========== 좋아요 관련 API ==========

    /**
     * 게시글 좋아요 토글
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeResponseDto> toggleLike(@PathVariable Long postId, HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            LikeResponseDto response = postLikeService.toggleLike(postId, currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LikeResponseDto.builder()
                    .liked(false)
                    .likeCount(0)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 게시글 좋아요 상태 확인
     */
    @GetMapping("/{postId}/like")
    public ResponseEntity<LikeResponseDto> getLikeStatus(@PathVariable Long postId, HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            boolean isLiked = postLikeService.isLikedByUser(postId, currentUserId);
            long likeCount = postLikeService.getLikeCount(postId);
            
            return ResponseEntity.ok(LikeResponseDto.builder()
                    .liked(isLiked)
                    .likeCount(likeCount)
                    .message(isLiked ? "좋아요를 눌렀습니다." : "좋아요를 누르지 않았습니다.")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LikeResponseDto.builder()
                    .liked(false)
                    .likeCount(0)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    // ========== 댓글 관련 API ==========

    /**
     * 댓글 작성
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(@PathVariable Long postId, 
                                                           @RequestBody CommentRequestDto requestDto,
                                                           HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            CommentResponseDto response = commentService.createComment(postId, requestDto, currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 게시글 댓글 목록 조회
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long postId) {
        try {
            List<CommentResponseDto> comments = commentService.getCommentsByPostId(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@PathVariable Long commentId,
                                                           @RequestBody CommentRequestDto requestDto,
                                                           HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            CommentResponseDto response = commentService.updateComment(commentId, requestDto, currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            commentService.deleteComment(commentId, currentUserId);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * 사용자 댓글 목록 조회
     */
    @GetMapping("/comments/user/{userId}")
    public ResponseEntity<List<CommentResponseDto>> getUserComments(@PathVariable Long userId) {
        try {
            List<CommentResponseDto> comments = commentService.getCommentsByUserId(userId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
