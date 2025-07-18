package com.commentarium.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(length = 5000)
    private String content;

    private Long postId;

    @Column(nullable = false)
    private List<Long> likes = new ArrayList<Long>();

    private Date createdAt;

    private Date updatedAt;

    // Self-reference to parent comment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    // Logic to ensure only top-level comments can have replies
    @PrePersist
    @PreUpdate
    private void validateReplyDepth() {
        if (parent != null && parent.parent != null) {
            throw new IllegalStateException("Replies to replies are not allowed.");
        }
    }

    public boolean isTopLevel() {
        return parent == null;
    }
}
