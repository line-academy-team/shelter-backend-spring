package com.lineacademy.shelterbackendspring.service;

import com.lineacademy.shelterbackendspring.domain.entity.Shelter;
import com.lineacademy.shelterbackendspring.domain.enums.ShelterType;
import com.lineacademy.shelterbackendspring.repository.ShelterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ShelterService {

    private final ShelterRepository shelterRepository;

    @Transactional(readOnly = true)
    public Flux<Shelter> getSheltersInBounds(Double minLat, Double maxLat, Double minLng, Double maxLng, Collection<ShelterType> types) {
        return Mono.fromCallable(() -> {
                    Collection<ShelterType> targetTypes = (types != null && !types.isEmpty()) ? types : null;
                    return shelterRepository.findWithinBounds(minLat, maxLat, minLng, maxLng, targetTypes);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    @Transactional(readOnly = true)
    public Mono<Shelter> getShelterById(Long id) {
        return Mono.fromCallable(() -> shelterRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쉼터입니다.")))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
