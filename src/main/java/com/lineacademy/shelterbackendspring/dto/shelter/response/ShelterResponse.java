package com.lineacademy.shelterbackendspring.dto.shelter.response;

import com.lineacademy.shelterbackendspring.domain.entity.Shelter;
import com.lineacademy.shelterbackendspring.domain.enums.ShelterType;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class ShelterResponse {
    private Long id;
    private String name;
    private Set<ShelterType> shelterTypes;
    private String facilityType;
    private String roadAddress;
    private String lotAddress;
    private Double latitude;
    private Double longitude;
    private Integer capacity;
    private String operatingHours;
    private String remark;

    public static ShelterResponse from(Shelter shelter) {
        return ShelterResponse.builder()
                .id(shelter.getId())
                .name(shelter.getName())
                .shelterTypes(shelter.getShelterTypes())
                .facilityType(shelter.getFacilityType())
                .roadAddress(shelter.getRoadAddress())
                .lotAddress(shelter.getLotAddress())
                .latitude(shelter.getLatitude())
                .longitude(shelter.getLongitude())
                .capacity(shelter.getCapacity())
                .operatingHours(shelter.getOperatingHours())
                .remark(shelter.getRemark())
                .build();
    }
}
