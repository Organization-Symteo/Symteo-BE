package com.symteo.domain.counsel.repository;

import com.symteo.domain.counsel.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounselRepository extends JpaRepository<ChatRoom, Long> {
}

