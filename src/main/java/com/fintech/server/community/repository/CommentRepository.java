package com.fintech.server.community.repository;

import com.fintech.server.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 특정 게시글의 모든 댓글을 조회합니다 (부모 댓글만, 최신순).
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL AND c.status = 'ACTIVE' ORDER BY c.createdAt DESC")
    List<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(@Param("postId") Long postId);
    
    /**
     * 특정 게시글의 모든 댓글을 조회합니다 (부모 댓글만, 최신순).
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL AND c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(@Param("postId") Long postId);
    
    /**
     * 특정 댓글의 모든 답글을 조회합니다 (최신순).
     */
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId AND c.status = 'ACTIVE' ORDER BY c.createdAt ASC")
    List<Comment> findByParentCommentIdAndStatusOrderByCreatedAtAsc(@Param("parentCommentId") Long parentCommentId);
    
    /**
     * 특정 게시글의 댓글 수를 조회합니다.
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.status = :status")
    long countByPostIdAndStatus(@Param("postId") Long postId, @Param("status") String status);
    
    /**
     * 특정 사용자가 작성한 모든 댓글을 조회합니다.
     */
    @Query("SELECT c FROM Comment c WHERE c.author.id = :authorId AND c.status = :status ORDER BY c.createdAt DESC")
    List<Comment> findByAuthorIdAndStatusOrderByCreatedAtDesc(@Param("authorId") Long authorId, @Param("status") String status);
    
    /**
     * 특정 댓글을 조회합니다 (작성자 확인용).
     */
    @Query("SELECT c FROM Comment c WHERE c.id = :commentId AND c.status = :status")
    Optional<Comment> findByIdAndStatus(@Param("commentId") Long commentId, @Param("status") String status);
    
    /**
     * 특정 게시글의 모든 댓글을 삭제합니다.
     * 게시글 삭제 시 사용됩니다.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
    
    /**
     * 특정 사용자가 작성한 모든 댓글을 삭제합니다.
     * 사용자 삭제 시 사용됩니다.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.author.id = :authorId")
    void deleteByAuthorId(@Param("authorId") Long authorId);
}
