package com.hobbyhop.domain.post.entity;


import com.hobbyhop.domain.BaseEntity;
import com.hobbyhop.domain.club.entity.Club;
import com.hobbyhop.domain.comment.entity.Comment;
import com.hobbyhop.domain.user.entity.User;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE post SET deleted_at = NOW() where id=?")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String postTitle;
    private String postContent;
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    public void changeTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public void changeContent(String postContent) {
        this.postContent = postContent;
    }

    public void changeImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}