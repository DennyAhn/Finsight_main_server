package com.fintech.server.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class PostTagLinkId implements Serializable {
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "tag_id")
    private Long tagId;
}
