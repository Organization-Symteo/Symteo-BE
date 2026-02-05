package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.converter.CounselConverter;
import com.symteo.domain.counsel.converter.CounselMessageConverter;
import com.symteo.domain.counsel.dto.ai.AISummaryDTO;
import com.symteo.domain.counsel.dto.ai.AiAnswerDTO;
import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.entity.ChatMessage;
import com.symteo.domain.counsel.entity.ChatRoom;
import com.symteo.domain.counsel.entity.CounselorSettings;
import com.symteo.domain.counsel.enums.Role;
import com.symteo.domain.counsel.exception.code.CounselErrorCode;
import com.symteo.domain.counsel.exception.code.CounselException;
import com.symteo.domain.counsel.repository.ChatMessageRepository;
import com.symteo.domain.counsel.repository.ChatRoomRepository;
import com.symteo.domain.counsel.repository.CounselorSettingRepository;
import com.symteo.domain.report.dto.ReportsResponse;
import com.symteo.domain.report.service.AttachmentReportsService;
import com.symteo.domain.report.service.DepressionAnxietyReportsService;
import com.symteo.domain.report.service.StressReportsService;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CounselCommandServiceImpl implements CounselCommandService{

    private final ChatClient chatClient;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CounselorSettingRepository counselorSettingRepository;
    private final PromptFactory promptFactory;

    @Value("classpath:prompts/askCounselPrompt.st")
    private Resource askCounselPrompt;

    /// --- 1. 상담 요청 메소드
    @Override
    @Transactional
    public CounselResDTO.ChatMessage askCounsel(Long userId, CounselReqDTO.ChatMessage dto) {

        String question = dto.text();

        ChatRoom chatRoom = (dto.chatRoomId() == null)
                ? chatRoomRepository.save(CounselConverter.toChatRoom(userId))
                : chatRoomRepository.findById(dto.chatRoomId())
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATROOM_NOT_FOUND));

        // 1) 이전 상담 내역 호출
        List<ChatMessage> readMessages = chatMessageRepository.getRecentMessages(userId, PageRequest.of(0, 10))
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATMESSAGE_NOT_FOUND));

        // 2) 사용자 상담 설정 로딩
        CounselorSettings settings = counselorSettingRepository.findById(userId)
                .orElseThrow(() -> new CounselException(CounselErrorCode._SETTING_NOT_FOUND));

        // 2-1) 프롬프트 로딩 => Resources 폴더에 존재
        String systemText = new SystemPromptTemplate(askCounselPrompt)
                .render(Map.of(
                        "atmosphere", settings.getAtmosphere(),
                        "support_style", settings.getSupportStyle(),
                        "role", settings.getRoleCounselor(),
                        "answer_format", settings.getAnswerFormat().getPromptText(),
                        "tone", settings.getTone()
                ));

        // 3) AI 호출
        AiAnswerDTO.AnswerDTO answer;
        try {
            answer = chatClient.prompt()
                    .system(systemText)
                    .messages(CounselMessageConverter.toAiMessages(readMessages))
                    .user(question)
                    .call()
                    .entity(AiAnswerDTO.AnswerDTO.class);
        } catch (Exception e) {
            log.error("AI 호출 중 오류 발생: ", e);
            throw new CounselException(CounselErrorCode._AI_SERVER_ERROR);
        }

        // 4) 사용자 질문 + AI 답변 저장
        ChatMessage chatMessage = CounselConverter.toChatMessage(question, chatRoom);
        chatMessageRepository.save(chatMessage);

        ChatMessage AIMessage = CounselConverter.toChatMessage(Objects.requireNonNull(answer).content(), chatRoom);
        AIMessage.setRole(Role.AI);
        chatMessageRepository.save(AIMessage);

        // 5) 결과 반환
        return CounselConverter.EntityToChatSet(chatRoom, question, answer.content());
    }

    /// --- 2. 리포트 분석 요청 메소드
    @Override
    public CounselResDTO.ChatMessage askReport(Long userId, CounselReqDTO.ChatReport dto) {

        ChatRoom chatRoom = (dto.chatRoomId() == null)
                ? chatRoomRepository.save(CounselConverter.toChatRoom(userId))
                : chatRoomRepository.findById(dto.chatRoomId())
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATROOM_NOT_FOUND));

        // 1) 진단 타입 확인, 리포트 가져오기
        String systemText = switch (dto.reportType()) {
            case ATTACHMENT_TEST -> promptFactory.createAPrompt(userId, dto.reportId());
            case STRESS_BURNOUT_COMPLEX -> promptFactory.createSBPrompt(userId, dto.reportId());
            case DEPRESSION_ANXIETY_COMPLEX -> promptFactory.createDAPrompt(userId, dto.reportId());
        };

        // 2) 이전 상담 내역 호출
        List<ChatMessage> readMessages = chatMessageRepository.getRecentMessages(userId, PageRequest.of(0, 10))
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATMESSAGE_NOT_FOUND));

        // 3) AI 리포트 분석
        String question = dto.reportType() + " 리포트 분석 결과에 대해서 알려줘.";
        AiAnswerDTO.AnswerDTO answer;
        try {
            answer = chatClient.prompt()
                    .system(systemText)
                    .messages(CounselMessageConverter.toAiMessages(readMessages))
                    .user(question)
                    .call()
                    .entity(AiAnswerDTO.AnswerDTO.class);
        } catch (Exception e) {
            log.error("AI 리포트 분석 중 오류 발생: ", e);
            throw new CounselException(CounselErrorCode._AI_SERVER_ERROR);
        }

        // 3) 결과 반환
        ChatMessage ReportMessage = CounselConverter.toChatMessage(Objects.requireNonNull(answer).content(), chatRoom);
        ReportMessage.setRole(Role.REPORT);
        chatMessageRepository.save(ReportMessage);

        return CounselConverter.EntityToChatSet(chatRoom, question, answer.content());
    }

    /// --- 3. 상담 종료 및 요약 메소드
    // 전체 채팅, AI 채팅, 유저 채팅을 각각 요약한다.
    @Transactional
    @Override
    public CounselResDTO.ChatSummary summaryCounsel(Long userId, CounselReqDTO.ChatSummary dto) {
        // 1) 채팅방 찾기
        ChatRoom chatRoom = chatRoomRepository.findById(dto.chatRoomId())
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATROOM_NOT_FOUND));

        // 2) 채팅 내역 가져오기
        List<ChatMessage> chatMessages = chatRoom.getChatMessages();
        List<ChatMessage> aiMessages = chatMessageRepository.findAllByChatRoom_ChatroomIdAndRole(dto.chatRoomId(), Role.AI)
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATMESSAGE_NOT_FOUND));
        List<ChatMessage> userMessages = chatMessageRepository.findAllByChatRoom_ChatroomIdAndRole(dto.chatRoomId(), Role.USER)
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATMESSAGE_NOT_FOUND));

        // 3) 프롬프트 설정
        String promptText = """
            당신은 전문 심리 상담 요약가입니다. 아래 제공된 세 가지 메시지 그룹을 각각 분석하여 요약해주세요.
            [지시 사항]
            1. 모든 요약은 15자 이내의 짧은 문장으로 작성하세요.
            2. 반드시 '명사'로 끝나도록 작성하세요. (예: "심리적 불안감 호소", "휴식 및 상담 권고")
            3. JSON의 모든 필드(chatMessages, userMessages, aiMessages)를 반드시 채워주세요.
        """;

        String userText = """
            [데이터]
            - 전체 대화: {chatMessages}
            - 사용자 메시지: {userMessages}
            - AI 메시지: {aiMessages}
        """;

        AISummaryDTO.ChatMessage summary;
        try {
            summary = chatClient.prompt()
                    .system(promptText)
                    .user(u -> u.text(userText)
                            .param("chatMessages", formatMessages(chatMessages))
                            .param("userMessages", formatMessages(userMessages))
                            .param("aiMessages", formatMessages(aiMessages))
                    )
                    .call()
                    .entity(AISummaryDTO.ChatMessage.class);
        } catch (Exception e) {
            log.error("Spring AI call failed", e);
            throw new CounselException(CounselErrorCode._AI_SERVER_ERROR);
        }

        // 4) AI 요약 내용 ChatRooms에 저장하기
        chatRoom.setChatSummary(Objects.requireNonNull(summary).chatMessages());
        chatRoom.setUserSummary(Objects.requireNonNull(summary).userMessages());
        chatRoom.setAiSummary(Objects.requireNonNull(summary).aiMessages());

        // 5) 반환
        return CounselConverter.EntityToChatSummary(chatRoom);
    }

    /// 2-1. 사용자 요청 전처리 메소드
    private String formatMessages(List<ChatMessage> messages) {
        return messages.stream()
                .map(m -> String.format("[%s]: %s", m.getRole(), m.getMessage()))
                .collect(Collectors.joining("\n"));
    }

    /// 3. 상담 삭제 메소드
    @Transactional
    @Override
    public Long deleteChat(Long userId, Long courseId) {
        ChatRoom chatRoom = chatRoomRepository.findById(courseId)
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATROOM_NOT_FOUND));

        if(!Objects.equals(chatRoom.getUserId(), userId)){ // null 안전 eqauls
            throw new CounselException(CounselErrorCode._CHATROOM_ACCESS_DENIED);
        }

        chatRoomRepository.delete(chatRoom);
        return courseId;
    }
}
