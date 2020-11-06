package com.watchers.service;

import com.watchers.model.environment.World;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SaveService {

    public void saveWorld(@NonNull World world){
        return;
    }

    public boolean exist(@NonNull Long aAlong){
        return false;
    }

    public Optional<World> findById(@NonNull Long id) {
        return Optional.empty();
    }
}
