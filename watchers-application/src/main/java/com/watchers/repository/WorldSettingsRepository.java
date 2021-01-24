package com.watchers.repository;

import com.watchers.model.world.WorldSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldSettingsRepository extends JpaRepository<WorldSetting, Long> {
}
