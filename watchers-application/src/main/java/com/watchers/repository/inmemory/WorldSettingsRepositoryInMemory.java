package com.watchers.repository.inmemory;

import com.watchers.model.WorldSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorldSettingsRepositoryInMemory extends JpaRepository<WorldSettings, Long>, JpaSpecificationExecutor<WorldSettings> {
}
