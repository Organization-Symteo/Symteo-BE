package com.symteo.domain.counsel.repository;

import com.symteo.domain.counsel.entity.CounselorSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounselorSettingRepository extends JpaRepository<CounselorSettings, Long> {
}