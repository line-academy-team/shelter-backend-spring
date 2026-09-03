package com.lineacademy.shelterbackendspring.domain.entity;

import com.lineacademy.shelterbackendspring.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "bookmarks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_shelter", columnNames = {"user_id", "shelter_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelter_id", nullable = false)
    private Shelter shelter;

    @Builder
    private Bookmark(User user, Shelter shelter) {
        this.user = user;
        this.shelter = shelter;
    }
}