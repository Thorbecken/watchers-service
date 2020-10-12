package com.watchers.repository.inmemory;

import com.watchers.model.environment.Continent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ContinentRepositoryInMemory extends JpaRepository<Continent, Long>, JpaSpecificationExecutor<Continent> {
}
