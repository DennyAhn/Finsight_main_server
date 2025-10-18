package com.fintech.server.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {
    private String body;
    private Long parentCommentId; // 답글인 경우 부모 댓글 ID
}
