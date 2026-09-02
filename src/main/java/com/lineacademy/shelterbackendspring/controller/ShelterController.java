package com.lineacademy.shelterbackendspring.controller;

import com.lineacademy.shelterbackendspring.domain.enums.ShelterType;
import com.lineacademy.shelterbackendspring.dto.shelter.response.ShelterResponse;
import com.lineacademy.shelterbackendspring.service.ShelterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shelters")
@RequiredArgsConstructor
public class ShelterController {

    private final ShelterService shelterService;

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getShelters(
            @RequestParam Double minLat,
            @RequestParam Double maxLat,
            @RequestParam Double minLng,
            @RequestParam Double maxLng,
            @RequestParam(required = false) List<ShelterType> types) {

        return shelterService.getSheltersInBounds(minLat, maxLat, minLng, maxLng, types)
                .map(ShelterResponse::from)
                .collectList()
                .map(list -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", list);
                    return ResponseEntity.ok(response);
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Map<String, Object>>> getShelter(@PathVariable Long id) {
        return shelterService.getShelterById(id)
                .map(shelter -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", ShelterResponse.from(shelter));
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }
}
