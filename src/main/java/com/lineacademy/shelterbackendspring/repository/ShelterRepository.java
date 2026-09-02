package com.lineacademy.shelterbackendspring.repository;

import com.lineacademy.shelterbackendspring.domain.entity.Shelter;
import com.lineacademy.shelterbackendspring.domain.enums.ShelterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShelterRepository extends JpaRepository<Shelter, Long> {
    Optional<Shelter> findByExternalIdAndShelterType(String externalId, ShelterType shelterType);
}
