package com.fintech.server.community.repository;

import com.fintech.server.community.entity.PostLike;
import com.fintech.server.community.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    
    /**
     * 특정 게시글의 좋아요 수를 조회합니다.
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.postId = :postId")
    long countByPostId(@Param("postId") Long postId);
    
    /**
     * 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인합니다.
     */
    @Query("SELECT COUNT(pl) > 0 FROM PostLike pl WHERE pl.userId = :userId AND pl.postId = :postId")
    boolean existsByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
    
    /**
     * 특정 사용자가 특정 게시글에 누른 좋아요를 삭제합니다.
     */
    @Modifying
    @Transactional
    void deleteByUserIdAndPostId(Long userId, Long postId);
    
    /**
     * 특정 사용자가 좋아요한 모든 게시글 ID를 조회합니다.
     */
    @Query("SELECT pl.postId FROM PostLike pl WHERE pl.userId = :userId")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 특정 게시글의 모든 좋아요를 삭제합니다.
     * 게시글 삭제 시 사용됩니다.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PostLike pl WHERE pl.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
    
    /**
     * 특정 사용자가 작성한 게시글의 모든 좋아요를 삭제합니다.
     * 사용자 삭제 시 사용됩니다.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PostLike pl WHERE pl.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
