package com.fintech.server.community.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PostRequestDto {
    private String body;
    private List<String> tags;
}
