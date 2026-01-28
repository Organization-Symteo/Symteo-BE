package com.symteo.domain.report.repository;

import com.symteo.domain.report.entity.mapping.AttachmentReports;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentReportsRepository extends JpaRepository<AttachmentReports, Long> {
}
