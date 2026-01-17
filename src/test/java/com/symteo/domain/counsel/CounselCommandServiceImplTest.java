package com.symteo.domain.counsel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.entity.ChatRoom;
import com.symteo.domain.counsel.repository.ChatMessageRepository;
import com.symteo.domain.counsel.repository.ChatRoomRepository;
import com.symteo.domain.counsel.service.CounselCommandServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CounselCommandServiceImplTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatClient chatClient;

    @InjectMocks
    private CounselCommandServiceImpl counselService;

    @Test
    @DisplayName("상담 질문 시 새로운 채팅방을 생성하고 AI 답변을 저장한다")
    void askCounsel_WithNewChatRoom() {
        // given
        CounselReqDTO.ChatMessage reqDto = new CounselReqDTO.ChatMessage(1L, null, "안녕하세요 AI님!");
        ChatRoom mockChatRoom = ChatRoom.builder().chatroom_id(100L).build();

        // ChatClient 모킹 (Fluent API 대응을 위해 Deep Stubs 설정처럼 동작하게 함)
        ChatClient.ChatClientRequestSpec mockSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec mockResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatRoomRepository.save(any())).thenReturn(mockChatRoom);

        // chatClient.prompt().user().call().content() 체이닝 모킹
        when(chatClient.prompt()).thenReturn(mockSpec);
        when(mockSpec.user(anyString())).thenReturn(mockSpec);
        when(mockSpec.call()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.content()).thenReturn("모킹된 AI 답변입니다.");

        // when
        CounselResDTO.ChatMessage result = counselService.askCounsel(reqDto);

        // then
        assertNotNull(result);
        assertEquals("모킹된 AI 답변입니다.", result.text()); // 반환된 답변 확인
        verify(chatRoomRepository, times(1)).save(any()); // 채팅방 저장 호출 확인
        verify(chatMessageRepository, times(2)).save(any()); // 유저 질문 + AI 답변 총 2번 저장 확인
    }
}
