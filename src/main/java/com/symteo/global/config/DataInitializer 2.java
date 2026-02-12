package com.symteo.global.config;

import com.symteo.domain.report.entity.mapping.Strength;
import com.symteo.domain.report.entity.mapping.StressPoints;
import com.symteo.domain.report.repository.StressPointsRepository;
import com.symteo.domain.report.repository.StrengthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final StressPointsRepository stressRepository;
    private final StrengthRepository strengthRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
        if (stressRepository.count() == 0) {
            stressRepository.saveAll(List.of(
                    // 안정형
                    StressPoints.builder().attachmentType("안정형").title("갑작스러운 관계의 단절이나 변화").stContents("예기치 못한 이별이나 소통의 부재는 안정형에게도 큰 충격입니다. 관계의 영속성을 믿기에, 그 믿음이 깨질 때 깊은 상실감을 느낍니다.").build(),
                    StressPoints.builder().attachmentType("안정형").title("상호 존중이 결여된 일방적인 소통").stContents("서로를 존중하는 대화를 중요하게 생각합니다. 비난이나 무시가 섞인 일방적인 소통 방식은 관계에 대한 회의감을 느끼게 합니다.").build(),
                    // 불안형
                    StressPoints.builder().attachmentType("불안형").title("상대방의 연락이 늦어지는 상황").stContents("연락의 부재를 단순한 바쁨이 아닌 '나에 대한 거절'로 해석하곤 합니다. 불확실한 기다림 속에서 버려질지도 모른다는 근본적인 불안이 커집니다.").build(),
                    StressPoints.builder().attachmentType("불안형").title("나의 감정을 무시당한다고 느낄 때").stContents("나의 서운함이나 슬픔을 '예민하다'고 치부당할 때 가장 큰 상처를 받습니다. 연결감을 잃었다는 생각에 더 강하게 감정을 호소하게 됩니다.").build(),
                    // 거부 회피형
                    StressPoints.builder().attachmentType("거부 회피형").title("누군가 나의 사생활에 깊이 개입할 때").stContents("혼자만의 공간과 시간을 침범당할 때 숨이 막히는 느낌을 받습니다. 타인의 의존이 심해지면 자신의 자유가 구속된다고 느껴 거부감을 나타냅니다.").build(),
                    StressPoints.builder().attachmentType("거부 회피형").title("감정적인 호소나 무거운 책임감 부여").stContents("상대방이 눈물이나 격한 감정으로 호소할 때 어떻게 대응할지 몰라 당혹스러워합니다. 무거운 정서적 책임감이 지워지는 상황을 회피하고 싶어 합니다.").build(),
                    // 공포 회피형
                    StressPoints.builder().attachmentType("공포 회피형").title("믿었던 사람에게 거절 당하는 경험").stContents("어렵게 마음을 열었는데 돌아온 거절은 세상이 무너지는 경험입니다. '역시 나는 사랑받을 수 없어'라는 부정적 믿음을 강화하며 깊은 동굴로 숨게 됩니다.").build(),
                    StressPoints.builder().attachmentType("공포 회피형").title("관계가 너무 가까워질 때 느끼는 두려움").stContents("관계가 너무 가까워지면 오히려 불안해집니다. 상대가 나를 깊이 알게 되었을 때 실망해서 떠나갈까 봐, 선제적으로 거리를 두며 자신을 보호하려 합니다.").build()
            ));
        }

        if (strengthRepository.count() == 0) {
            strengthRepository.saveAll(List.of(
                    // 안정형
                    Strength.builder().attachmentType("안정형").title("타인과 건강한 경계를 유지하는 능력").strengthContents("'나'와 '너'의 경계를 명확히 알고 있습니다. 상대에게 의존하지 않으면서도 필요할 땐 도움을 주고받는 건강한 거리 조절 능력이 탁월합니다.").build(),
                    Strength.builder().attachmentType("안정형").title("갈등 상황에서 유연하게 대처하는 여유").strengthContents("갈등이 생겨도 상대를 적대시하지 않습니다. 감정에 휘둘리기보다 문제의 원인을 찾아 대화로 해결하려는 높은 정서적 지능을 보유하고 있습니다.").build(),
                    // 불안형
                    Strength.builder().attachmentType("불안형").title("섬세한 감수성으로 상대의 니즈 파악").strengthContents("타인의 아주 미묘한 표정 변화나 목소리 톤도 포착해냅니다. 상대가 무엇을 원하는지 먼저 알아차리고 배려하는 따뜻한 공감 능력을 갖추고 있습니다.").build(),
                    Strength.builder().attachmentType("불안형").title("관계를 유지하기 위한 높은 헌신도").strengthContents("사랑하는 사람을 위해 아낌없이 에너지를 쏟습니다. 관계를 유지하고 개선하기 위해 누구보다 먼저 노력하며, 깊은 유대감을 형성하는 데 탁월합니다.").build(),
                    // 거부 회피형
                    Strength.builder().attachmentType("거부 회피형").title("객관적이고 독립적인 문제 해결 능력").strengthContents("타인에게 의존하지 않고 스스로 문제를 해결하는 능력이 뛰어납니다. 외부의 비난이나 압박에도 흔들리지 않고 자신의 길을 가는 뚝심이 있습니다.").build(),
                    Strength.builder().attachmentType("거부 회피형").title("감정에 휘둘리지 않는 차분한 판단력").strengthContents("위기 상황에서 감정에 매몰되지 않습니다. 한 발자국 물러나 상황을 객관적으로 분석하며, 합리적인 대안을 제시하는 능력이 돋보입니다.").build(),
                    // 공포 회피형
                    Strength.builder().attachmentType("공포 회피형").title("타인의 복합적인 감정을 이해하는 깊이").strengthContents("복잡하고 모순적인 감정의 결을 누구보다 잘 이해합니다. 상처받은 영혼에 대한 깊은 연민을 가지고 있어, 타인에게 진정성 있는 위로를 건넬 줄 압니다.").build(),
                    Strength.builder().attachmentType("공포 회피형").title("자기 성찰을 통한 높은 내면 인식 능력").strengthContents("늘 자신의 감정을 살피고 분석하기에 내면의 흐름에 민감합니다. 이러한 자기 성찰은 치유의 과정을 거치면 누구보다 단단한 내적 성장을 이뤄내는 밑거름이 됩니다.").build()
            ));
        }
    }
}