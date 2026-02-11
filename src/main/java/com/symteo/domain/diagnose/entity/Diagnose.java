package com.symteo.domain.diagnose.entity;

import com.symteo.domain.diagnose.dto.req.DiagnoseReqDTO;
import com.symteo.domain.diagnose.enums.DiagnoseType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "diagnoses")
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Diagnose {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "test_type")
    @Enumerated(EnumType.STRING)
    private DiagnoseType testType; // 검사 유형

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<DiagnoseReqDTO.AnswerDTO> answers; // 상세 응답 (JSON)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
