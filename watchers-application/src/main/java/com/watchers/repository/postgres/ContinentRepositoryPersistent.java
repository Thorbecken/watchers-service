package com.watchers.repository.postgres;

import com.watchers.model.environment.Continent;
import com.watchers.model.environment.World;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContinentRepositoryPersistent extends CrudRepository<Continent, Long>, JpaSpecificationExecutor<Continent> {
        }