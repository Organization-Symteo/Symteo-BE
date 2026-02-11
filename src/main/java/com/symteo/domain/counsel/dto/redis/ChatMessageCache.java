package com.symteo.domain.counsel.dto.redis;

import com.symteo.domain.counsel.enums.Role;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public record ChatMessageCache(
        Long chatRoomId,
        Role role,
        String content,
        LocalDateTime createdAt
) implements Serializable {
}
