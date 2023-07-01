package com.watchers.repository;

import com.watchers.model.coordinate.Coordinate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoordinateRepository extends JpaRepository<Coordinate, Long> {

    @Query("Select c from Coordinate c where c.xCoord = :xCoord and c.yCoord = :yCoord")
    Optional<Coordinate> findByCoordinates(@Param("xCoord") Long xCoord, @Param("yCoord") Long yCoord);

}
