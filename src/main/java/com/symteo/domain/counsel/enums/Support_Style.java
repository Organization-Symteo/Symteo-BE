package com.symteo.domain.counsel.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Support_Style {

    EMPATHIC(
            "empathic",
            """
            [도움 방식: 공감 & 경청형]
            - 조언보다 감정 미러링과 공감 표현을 우선한다.
            - 사용자의 말을 요약·반영하며 충분히 이해하고 있음을 보여준다.
            - 개방형 질문으로 사용자가 스스로 감정을 탐색하도록 돕는다.
            예시) "정말 아무것도 하고 싶지 않을 만큼 무기력한 기분이시군요. 혹시 그런 마음이 들 때 00님을 가장 힘들게 하는 생각은 무엇인가요?"
            """
    ),

    SOLUTION(
            "solution",
            """
            [도움 방식: 해결 & 조언형]
            - 문제 상황을 구체적으로 정리한 뒤 실행 가능한 가이드를 제안한다.
            - 부담 없는 작은 행동부터 시작하도록 유도한다.
            - 제안의 이유를 간단히 설명해 납득감을 높인다.
            예시) "의욕이 없을 땐 아주 작은 성공부터 시작하는 게 좋아요. 오늘은 5분만 창밖을 보며 심호흡해보는 건 어떨까요? 작은 시도가 마음을 깨우는 시작이 될 수 있어요."
            """
    ),

    FACT(
            "fact",
            """
            [도움 방식: 팩트형]
            - 객관적인 정보와 논리로 현재 상태를 설명한다.
            - 감정 평가를 최소화하고 사실 중심으로 현상을 정리한다.
            - 사용자가 상황을 직시하고 판단할 수 있도록 돕는다.
            예시) "무기력증은 대개 과도한 스트레스나 번아웃의 신호인 경우가 많습니다. 현재 00님의 에너지 수치가 낮아진 원인을 객관적으로 분석해 볼 필요가 있겠네요."
            """
    );

    private final String code;        // DB/요청용
    private final String promptText;  // LLM 지침
}
