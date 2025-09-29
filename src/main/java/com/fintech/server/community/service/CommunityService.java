package com.fintech.server.community.service;

import com.fintech.server.community.dto.PostRequestDto;
import com.fintech.server.community.dto.PostResponseDto;
import com.fintech.server.community.entity.CommunityPost;
import com.fintech.server.community.entity.PostTagLink;
import com.fintech.server.community.entity.PostTagLinkId;
import com.fintech.server.community.entity.Tag;
import com.fintech.server.community.repository.CommunityPostRepository;
import com.fintech.server.community.repository.PostTagLinkRepository;
import com.fintech.server.community.repository.TagRepository;
import com.fintech.server.entity.User;
import com.fintech.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagLinkRepository postTagLinkRepository;
    private final UserRepository userRepository;

    public PostResponseDto createPost(PostRequestDto requestDto, Long userId) {
        System.out.println("=== createPost 시작 ===");
        System.out.println("userId: " + userId);
        System.out.println("requestDto body: " + requestDto.getBody());
        System.out.println("requestDto tags: " + requestDto.getTags());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("User found: " + user.getNickname());

        CommunityPost post = new CommunityPost();
        post.setAuthor(user);
        post.setBody(requestDto.getBody());
        post.setAuthorBadge(user.getDisplayedBadge()); // 사용자의 현재 표시 배지 저장
        System.out.println("Post entity created");

        CommunityPost savedPost = postRepository.save(post);
        System.out.println("Post saved with ID: " + savedPost.getId());

        // 태그 처리
        if (requestDto.getTags() != null) {
            System.out.println("태그 처리 시작, 태그 개수: " + requestDto.getTags().size());
            for (String tagName : requestDto.getTags()) {
                System.out.println("태그 처리 중: " + tagName);
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            System.out.println("새 태그 생성: " + tagName);
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return tagRepository.save(newTag);
                        });
                System.out.println("태그 ID: " + tag.getId());

                PostTagLinkId linkId = new PostTagLinkId();
                linkId.setPostId(savedPost.getId());
                linkId.setTagId(tag.getId());

                PostTagLink link = new PostTagLink();
                link.setId(linkId);
                link.setPost(savedPost);
                link.setTag(tag);
                System.out.println("PostTagLink 저장 시도");
                postTagLinkRepository.save(link);
                System.out.println("PostTagLink 저장 완료");
            }
        }
        // 간단하게 저장된 포스트로 응답 생성 (태그는 별도로 처리)
        System.out.println("PostResponseDto 변환 시도");
        
        // 배지 정보
        PostResponseDto.BadgeDto badgeDto = null;
        if (savedPost.getAuthorBadge() != null) {
            badgeDto = PostResponseDto.BadgeDto.builder()
                    .name(savedPost.getAuthorBadge().getName())
                    .iconUrl(savedPost.getAuthorBadge().getIconUrl())
                    .build();
        }
        
        // 작성자 정보
        PostResponseDto.AuthorDto authorDto = PostResponseDto.AuthorDto.builder()
                .id(savedPost.getAuthor().getId())
                .nickname(savedPost.getAuthor().getNickname())
                .badge(badgeDto)
                .build();
        
        // 태그 정보 (저장한 태그들을 직접 조회)
        List<String> tagNames = new ArrayList<>();
        if (requestDto.getTags() != null) {
            tagNames.addAll(requestDto.getTags());
        }
        
        PostResponseDto result = PostResponseDto.builder()
                .id(savedPost.getId())
                .author(authorDto)
                .body(savedPost.getBody())
                .likeCount(savedPost.getLikeCount())
                .commentCount(savedPost.getCommentCount())
                .tags(tagNames)
                .createdAt(savedPost.getCreatedAt())
                .build();
                
        System.out.println("PostResponseDto 변환 완료");
        return result;
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> findAllPosts() {
        return postRepository.findAllWithAuthorAndBadge().stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }
}
