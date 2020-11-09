package com.watchers.repository;

import com.watchers.model.environment.Biome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BiomeRepository extends JpaRepository<Biome, Long> {
}
