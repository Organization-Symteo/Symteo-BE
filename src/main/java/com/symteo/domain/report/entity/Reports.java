package com.symteo.domain.report.entity;

import com.symteo.domain.user.entity.User;
import jakarta.persistence.*; // jakarta로 통일
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "reports")
public class Reports {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Column(name = "r_type")
    private String rType;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    public void complete() { this.completedAt = LocalDateTime.now(); }
}