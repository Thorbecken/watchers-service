package com.watchers.repository;

import com.watchers.model.WorldSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorldSettingsRepository extends JpaRepository<WorldSetting, Long> {
}
