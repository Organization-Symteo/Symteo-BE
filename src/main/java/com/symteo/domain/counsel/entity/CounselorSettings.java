package com.symteo.domain.counsel.entity;

import com.symteo.domain.counsel.enums.Answer_Format;
import com.symteo.domain.counsel.enums.Atmosphere;
import com.symteo.domain.counsel.enums.Counselor_Role;
import com.symteo.domain.counsel.enums.Support_Style;
import com.symteo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "AI_Counselor_Settings")
public class CounselorSettings {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "atmosphere", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Atmosphere atmosphere;

    @Column(name = "support_style", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Support_Style supportStyle;

    @Column(name = "role_counselor", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Counselor_Role roleCounselor;

    @Column(name = "answer_format", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Answer_Format answerFormat;

}
