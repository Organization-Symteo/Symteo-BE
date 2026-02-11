package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.converter.CounselConverter;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.entity.ChatRoom;
import com.symteo.domain.counsel.exception.code.CounselErrorCode;
import com.symteo.domain.counsel.exception.code.CounselException;
import com.symteo.domain.counsel.repository.ChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CounselQueryServiceImpl implements CounselQueryService{
    private final ChatRoomRepository chatRoomRepository;

    // 데이터베이스 전체 상담내역 조회
    @Override
    @Transactional
    public List<CounselResDTO.Chat> readAllChat(Long userId) {

        List<ChatRoom> allChats = chatRoomRepository.findAllByUserId(userId)
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATROOM_NOT_FOUND));

        List<CounselResDTO.Chat> chatList = allChats.stream()
                .map(CounselConverter::ChatRoomToChat)
                .toList();

        return chatList;
    }

    // 데이터베이스 단일 상담 조회
    @Override
    public CounselResDTO.ChatSummary readChat(Long userId, Long chatRoomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATROOM_NOT_FOUND));

        if(!Objects.equals(chatRoom.getUserId(), userId)){ // null 안전 eqauls
            throw new CounselException(CounselErrorCode._CHATROOM_ACCESS_DENIED);
        }

        return CounselConverter.EntityToChatDetails(chatRoom);
    }
}
