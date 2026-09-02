package com.lineacademy.shelterbackendspring.repository;

import com.lineacademy.shelterbackendspring.domain.entity.Shelter;
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
}
