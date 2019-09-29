package com.watchers.repository;

import com.watchers.model.World;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorldRepository extends CrudRepository<World, Long> {

/*    @Query()
    boolean worldExist(@Param("worldId") long worldId);*/
}
