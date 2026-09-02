package com.lineacademy.shelterbackendspring.repository;

import com.lineacademy.shelterbackendspring.domain.entity.Shelter;
import com.lineacademy.shelterbackendspring.domain.enums.ShelterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShelterRepository extends JpaRepository<Shelter, Long> {
    @Query("SELECT s FROM Shelter s WHERE s.name = :name " +
            "OR (ABS(s.latitude - :lat) < 0.0001 AND ABS(s.longitude - :lng) < 0.0001)")
    List<Shelter> findExistingShelters(@Param("name") String name,
                                       @Param("lat") Double lat,
                                       @Param("lng") Double lng);

    @Query("SELECT DISTINCT s FROM Shelter s " +
           "LEFT JOIN s.shelterTypes t " +
           "WHERE s.latitude BETWEEN :minLat AND :maxLat " +
           "AND s.longitude BETWEEN :minLng AND :maxLng " +
           "AND (:type IS NULL OR t = :type)")
    List<Shelter> findWithinBounds(@Param("minLat") Double minLat,
                                   @Param("maxLat") Double maxLat,
                                   @Param("minLng") Double minLng,
                                   @Param("maxLng") Double maxLng,
                                   @Param("type")ShelterType type);
}
