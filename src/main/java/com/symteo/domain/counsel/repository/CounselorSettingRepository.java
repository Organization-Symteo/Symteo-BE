package com.symteo.domain.counsel.repository;

import com.symteo.domain.counsel.entity.CounselorSettings;
import com.symteo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CounselorSettingRepository extends JpaRepository<CounselorSettings, Long> {
    Optional<CounselorSettings> findByUser(User user);
}