package com.watchers.repository.inmemory;

import com.watchers.model.environment.Biome;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface BiomeRepositoryInMemory extends CrudRepository<Biome, Long>, JpaSpecificationExecutor<Biome> {
}
