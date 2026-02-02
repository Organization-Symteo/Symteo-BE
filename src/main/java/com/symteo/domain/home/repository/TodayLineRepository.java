package com.symteo.domain.home.repository;

import com.symteo.domain.home.entity.TodayLines;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodayLineRepository extends JpaRepository<TodayLines, Long> {

}