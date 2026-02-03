package com.symteo.domain.home.entity;

import com.symteo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "today_emotion")
public class TodayEmotions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "to_emotion_id")
    private Long todayEmotionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Column(name = "to_emotion", nullable = false)
    private Integer emotion; // 1: 맑음, 2: 구름, 3: 번개, 4: 비

    @Column(name = "te_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TodayEmotions(User user, Integer emotion) {
        this.user = user;
        this.emotion = emotion;
        this.createdAt = LocalDateTime.now();
    }

    public void updateEmotion(Integer emotion) {
        this.emotion = emotion;
    }
}