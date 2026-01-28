package com.symteo.domain.report.entity.mapping;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "Stress_points")
public class StressPoints {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "st_point_id")
    private Long stPointId;

    @Column(name = "attachment_type")
    private String attachmentType;

    @Column(name = "title")
    private String title; // 예: "상대방의 연락이 늦어지는 상황"

    @Column(columnDefinition = "TEXT")
    private String stContents; // 상세 설명 문구
}