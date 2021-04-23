package com.watchers.repository;

import com.watchers.model.world.WorldMetaData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldMetaDataRepository extends JpaRepository<WorldMetaData, Long> {
}
