package com.watchers.repository.inmemory;

import com.watchers.model.environment.Continent;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface ContinentRepositoryInMemory extends CrudRepository<Continent, Long>, JpaSpecificationExecutor<Continent> {
}
