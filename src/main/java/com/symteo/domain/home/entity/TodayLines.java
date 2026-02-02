package com.symteo.domain.home.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "today_lines")
public class TodayLines {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "today_line_id")
    private Long todayLineId;

    @Column(name = "to_line_contents", nullable = false, columnDefinition = "TEXT")
    private String contents;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TodayLines(String contents) {
        this.contents = contents;
        this.createdAt = LocalDateTime.now();
    }
}