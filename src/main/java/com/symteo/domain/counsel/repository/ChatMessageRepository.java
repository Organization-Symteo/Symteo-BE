package com.symteo.domain.counsel.repository;

import com.symteo.domain.counsel.entity.ChatMessage;
import com.symteo.domain.counsel.enums.Role;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<List<ChatMessage>> findAllByChatRoom_ChatroomIdAndRole(Long chatRoomId, Role role);

    @Query("SELECT c FROM ChatMessage c WHERE c.chatRoom.userId = :userId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    Optional<List<ChatMessage>> getRecentMessages(
            @Param("userId") Long userId,
            PageRequest pageRequest
    );
}
