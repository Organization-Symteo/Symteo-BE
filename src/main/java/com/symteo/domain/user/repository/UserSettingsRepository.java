package com.symteo.domain.user.repository;

import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUser(User user);
    Optional<UserSettings> findByUser_Id(Long userId);
}

