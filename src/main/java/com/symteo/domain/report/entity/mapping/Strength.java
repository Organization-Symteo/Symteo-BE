package com.symteo.domain.report.entity.mapping;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "Strength")
public class Strength {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "strength_id")
    private Long strengthId;

    @Column(name = "attachment_type")
    private String attachmentType;

    @Column(name = "title")
    private String title; // 예: "섬세한 니즈 파악"

    @Column(columnDefinition = "TEXT")
    private String strengthContents; // 상세 설명 문구
}