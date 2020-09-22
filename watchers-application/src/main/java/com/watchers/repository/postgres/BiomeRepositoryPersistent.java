package com.watchers.repository.postgres;

import com.watchers.model.environment.Biome;
import com.watchers.model.environment.World;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BiomeRepositoryPersistent extends CrudRepository<Biome, Long>, JpaSpecificationExecutor<Biome> {
        }