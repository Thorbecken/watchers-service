package com.watchers.repository.inmemory;

import com.watchers.model.actor.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface ActorRepositoryInMemory extends JpaRepository<Actor, Long>, JpaSpecificationExecutor<Actor> {
}
