package com.watchers.repository.inmemory;

import com.watchers.model.actor.Actor;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface ActorRepositoryInMemory extends CrudRepository<Actor, Long>, JpaSpecificationExecutor<Actor> {
}
