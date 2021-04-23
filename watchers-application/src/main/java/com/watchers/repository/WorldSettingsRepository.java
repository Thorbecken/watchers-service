package com.watchers.repository;

import com.watchers.model.world.WorldSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldSettingsRepository extends JpaRepository<WorldSettings, Long> {
}
