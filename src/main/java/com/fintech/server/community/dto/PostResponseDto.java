package com.fintech.server.community.dto;

import com.fintech.server.community.entity.CommunityPost;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PostResponseDto {
    private Long id;
    private AuthorDto author;
    private String body;
    private int likeCount;
    private boolean liked;  // 사용자가 좋아요를 눌렀는지 여부
    private int commentCount;
    private List<String> tags;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class AuthorDto {
        private Long id;
        private String nickname;
        private BadgeDto badge;
    }

    @Getter
    @Builder
    public static class BadgeDto {
        private String name;
        private String iconUrl;
    }

    public static PostResponseDto from(CommunityPost post) {
        return from(post, false);
    }
    
    public static PostResponseDto from(CommunityPost post, boolean liked) {
        BadgeDto badgeDto = null;
        if (post.getAuthorBadge() != null) {
            badgeDto = BadgeDto.builder()
                    .name(post.getAuthorBadge().getName())
                    .iconUrl(post.getAuthorBadge().getIconUrl())
                    .build();
        }

        AuthorDto authorDto = AuthorDto.builder()
                .id(post.getAuthor().getId())
                .nickname(post.getAuthor().getNickname())
                .badge(badgeDto)
                .build();

        List<String> tagNames = post.getTags() != null ? 
                post.getTags().stream()
                        .map(postTagLink -> postTagLink.getTag().getName())
                        .collect(Collectors.toList()) : 
                new ArrayList<>();

        return PostResponseDto.builder()
                .id(post.getId())
                .author(authorDto)
                .body(post.getBody())
                .likeCount(post.getLikeCount())
                .liked(liked)
                .commentCount(post.getCommentCount())
                .tags(tagNames)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
