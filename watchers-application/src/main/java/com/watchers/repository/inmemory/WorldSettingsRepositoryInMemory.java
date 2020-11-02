package com.watchers.repository.inmemory;

import com.watchers.model.worldsetting.WorldSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorldSettingsRepositoryInMemory extends JpaRepository<WorldSetting, Long>, JpaSpecificationExecutor<WorldSetting> {
}
