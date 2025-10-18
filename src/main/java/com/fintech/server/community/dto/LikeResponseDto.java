package com.fintech.server.community.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikeResponseDto {
    private boolean liked;
    private long likeCount;
    private String message;
}
