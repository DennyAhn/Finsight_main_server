# 커뮤니티 API 문서

## 개요
이 문서는 핀테크 서버의 커뮤니티 기능 관련 API를 정리합니다.

## Base URL
```
/api/community/posts
```

## API 목록

### 1. 게시글 작성
**POST** `/api/community/posts`

#### 요청 헤더
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN} (선택사항)
```

#### 요청 본문
```json
{
  "body": "게시글 내용",
  "tags": ["태그1", "태그2", "태그3"]
}
```

#### 요청 본문 설명
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| body | String | 필수 | 게시글 내용 |
| tags | List<String> | 선택 | 태그 목록 |

#### 응답
```json
{
  "id": 1,
  "author": {
    "id": 64,
    "nickname": "사용자닉네임",
    "badge": {
      "name": "배지명",
      "iconUrl": "배지아이콘URL"
    }
  },
  "body": "게시글 내용",
  "likeCount": 0,
  "commentCount": 0,
  "tags": ["태그1", "태그2", "태그3"],
  "createdAt": "2024-01-01T12:00:00"
}
```

#### 응답 필드 설명
| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 게시글 ID |
| author | Object | 작성자 정보 |
| author.id | Long | 작성자 ID |
| author.nickname | String | 작성자 닉네임 |
| author.badge | Object | 작성자 배지 (nullable) |
| author.badge.name | String | 배지명 |
| author.badge.iconUrl | String | 배지 아이콘 URL |
| body | String | 게시글 내용 |
| likeCount | Integer | 좋아요 수 |
| commentCount | Integer | 댓글 수 |
| tags | List<String> | 태그 목록 |
| createdAt | LocalDateTime | 작성일시 |

#### 상태 코드
- `201 Created`: 게시글 작성 성공
- `500 Internal Server Error`: 서버 오류

---

### 2. 게시글 목록 조회
**GET** `/api/community/posts`

#### 요청 헤더
```
Content-Type: application/json
```

#### 응답
```json
[
  {
    "id": 1,
    "author": {
      "id": 64,
      "nickname": "사용자닉네임",
      "badge": {
        "name": "배지명",
        "iconUrl": "배지아이콘URL"
      }
    },
    "body": "게시글 내용",
    "likeCount": 0,
    "commentCount": 0,
    "tags": ["태그1", "태그2"],
    "createdAt": "2024-01-01T12:00:00"
  }
]
```

#### 상태 코드
- `200 OK`: 조회 성공

---

## 데이터 모델

### CommunityPost Entity
```java
@Entity
@Table(name = "community_posts")
public class CommunityPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_badge_id")
    private Badge authorBadge; // 작성 시점의 대표 배지 (스냅샷)
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;
    
    @Column(name = "like_count")
    private int likeCount = 0;
    
    @Column(name = "comment_count")
    private int commentCount = 0;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostTagLink> tags = new HashSet<>();
}
```

### Tag Entity
```java
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
}
```

### PostTagLink Entity
```java
@Entity
@Table(name = "post_tag_links")
public class PostTagLink {
    @EmbeddedId
    private PostTagLinkId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    private CommunityPost post;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    private Tag tag;
}
```

## 인증 및 권한
- JWT 토큰이 있는 경우: 토큰에서 사용자 ID 추출
- JWT 토큰이 없는 경우: 기본 게스트 사용자 ID (64L) 사용
- 모든 사용자가 게시글 작성 및 조회 가능

## 태그 시스템
- 태그는 자동으로 생성됩니다 (존재하지 않는 태그명인 경우)
- 태그명은 중복될 수 없습니다 (unique constraint)
- 게시글과 태그는 다대다 관계로 PostTagLink를 통해 연결됩니다

## 배지 시스템
- 게시글 작성 시점의 사용자 대표 배지가 스냅샷으로 저장됩니다
- 배지가 없는 사용자의 경우 null로 저장됩니다

## 오류 처리
- 사용자를 찾을 수 없는 경우: `RuntimeException("User not found")`
- 기타 서버 오류: `500 Internal Server Error` 응답