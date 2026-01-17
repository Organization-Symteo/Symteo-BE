package com.symteo.domain.counsel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoom {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatroom_id;

    //채팅 사용자
    @Column(name = "user_id", nullable = false)
    private Long userId;

    //채팅 요약
    @Column(name = "chatsummary")
    private String chatSummary;

    //연관 관계
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> chatMessages = new ArrayList<>();
}
