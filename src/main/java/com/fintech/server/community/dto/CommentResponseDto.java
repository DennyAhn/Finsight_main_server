package com.fintech.server.community.dto;

import com.fintech.server.community.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CommentResponseDto {
    private Long id;
    private AuthorDto author;
    private String body;
    private Long parentCommentId;
    private List<CommentResponseDto> replies;
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

    public static CommentResponseDto from(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .author(AuthorDto.builder()
                        .id(comment.getAuthor().getId())
                        .nickname(comment.getAuthor().getNickname())
                        .badge(comment.getAuthor().getDisplayedBadge() != null ? 
                                BadgeDto.builder()
                                        .name(comment.getAuthor().getDisplayedBadge().getName())
                                        .iconUrl(comment.getAuthor().getDisplayedBadge().getIconUrl())
                                        .build() : null)
                        .build())
                .body(comment.getBody())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .replies(comment.getReplies().stream()
                        .map(CommentResponseDto::from)
                        .collect(Collectors.toList()))
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
