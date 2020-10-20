package com.watchers.repository.postgres;

import com.watchers.model.actor.Actor;
import com.watchers.model.environment.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepositoryPersistent extends JpaRepository<Actor, Long>, JpaSpecificationExecutor<Actor> {
        }