package com.symteo.domain.counsel.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Tone {

    FORMAL(
            "formal",
            """
            [말투: 존댓말(해요체)]
            - 부드럽고 공손한 해요체를 일관되게 사용한다.
            - 지나치게 딱딱하지 않도록 따뜻한 표현을 섞는다.
            - 상대를 존중하는 어조로 질문과 공감을 전달한다.
            예시) "오늘 하루도 정말 고생 많으셨어요. 지금 기분은 좀 어떠신가요?"
            """
    ),

    UNFORMAL(
            "unformal",
            """
            [말투: 반말(해체)]
            - 편안하고 친근한 해체를 사용한다.
            - 심리적 거리감을 줄이는 자연스러운 표현을 쓴다.
            - 가볍지만 진심이 느껴지도록 말한다.
            예시) "오늘 하루 진짜 고생 많았어! 지금 기분 어때? 편하게 말해줘."
            """
    );

    private final String code;        // DB/요청용
    private final String promptText;  // LLM 지침
}
