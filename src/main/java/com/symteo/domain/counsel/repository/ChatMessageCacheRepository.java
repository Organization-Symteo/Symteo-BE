package com.symteo.domain.counsel.repository;

import com.symteo.domain.counsel.dto.redis.ChatMessageCache;
import com.symteo.domain.counsel.entity.CounselorSettings;
import com.symteo.domain.counsel.exception.code.CounselErrorCode;
import com.symteo.domain.counsel.exception.code.CounselException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class ChatMessageCacheRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String MESSAGE_KEY_PREFIX = "chat:room:%d"; // chatRoom{Id}의 키 형식
    private static final long TTL_MINUTES = 40;

    /// Redis key 생성자
    private String createKey(Long chatRoomId){
        return String.format(MESSAGE_KEY_PREFIX, chatRoomId);
    }

    // 1. 상담자 설정 작업
    /// 상담자 세팅 캐시 저장
    public void saveSetting(Long chatRoomId, CounselorSettings setting) {
        try {
            String key = createKey(chatRoomId);
            redisTemplate.opsForValue().set(key, setting, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new CounselException(CounselErrorCode._REDIS_SETTING_NOT_SAVED);
        }

    }

    /// 상담자 세팅 캐시 조회
    public CounselorSettings readSetting(Long chatRoomId) {
        CounselorSettings result = (CounselorSettings) Optional.ofNullable(redisTemplate.opsForValue().get(createKey(chatRoomId)))
                .orElseThrow(() -> new CounselException(CounselErrorCode._REDIS_SETTING_NOT_FOUND));
        return result;
    }

    // 2. 채팅 내역 작업
    /// Redis 캐시에 채팅 메시지 한 개 저장
    public void addChatMessage(ChatMessageCache message){
        try {
            String key = createKey(message.chatRoomId());
            redisTemplate.opsForList().rightPush(key, message);
            redisTemplate.expire(key, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new CounselException(CounselErrorCode._REDIS_CHATMESSAGE_NOT_SAVED);
        }
    }


    /// 전체 메시지 조회 - 채팅 종료 시 MySQL 이관용
    public List<ChatMessageCache> findAllByChatRoom(Long chatRoomId) {
        String key = createKey(chatRoomId);
        List<Object> objects = Optional.ofNullable(redisTemplate.opsForList().range(key, 0, -1))
                .orElseThrow(() -> new CounselException(CounselErrorCode._REDIS_CHATMESSAGE_NOT_FOUND));

        return objects.stream()
                .map(ChatMessageCache.class::cast)
                .toList();
    }

    /// 최근 N개 메시지 조회 - OpenAI 컨텍스트용
    public List<ChatMessageCache> getRecentMessages(Long chatRoomId, int count) {
        String key = createKey(chatRoomId);
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return Collections.emptyList();
        }
        long start = Math.max(0, size - count);
        List<Object> objects = redisTemplate.opsForList().range(key, start, -1);
        if (objects == null) {
            return Collections.emptyList();
        }
        return objects.stream()
                .map(ChatMessageCache.class::cast)
                .toList();
    }

    /// 메시지 수 조회
    public long count(Long chatRoomId) {
        Long size = redisTemplate.opsForList().size(createKey(chatRoomId));
        return size != null ? size : 0;
    }

    /// 채팅방 데이터 삭제 - MySQL 이관 완료 후
    public void delete(Long chatRoomId) {
        redisTemplate.delete(createKey(chatRoomId));
    }


    /// 존재 여부 확인
    public boolean exists(Long chatRoomId) {
        return redisTemplate.hasKey(createKey(chatRoomId));
    }

}
