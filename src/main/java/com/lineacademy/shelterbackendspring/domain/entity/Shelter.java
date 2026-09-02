package com.lineacademy.shelterbackendspring.domain.entity;

import com.lineacademy.shelterbackendspring.domain.common.BaseTimeEntity;

import com.lineacademy.shelterbackendspring.domain.enums.ShelterType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "shelters",
        indexes = {
                @Index(name = "idx_shelter_location", columnList = "latitude, longitude"),
                @Index(name = "idx_shelter_sync", columnList = "external_id, shelter_type", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shelter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 공공데이터 고유 식별자 (SNO, SN, AREA_CD+명칭 등)
    @Column(name = "external_id", nullable = false, length = 100)
    private String externalId;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "shelter_type", nullable = false, length = 20)
    private ShelterType shelterType;

    @Column(length = 50)
    private String facilityType; // 시설 구분 (특정계층, 공공시설, 노인시설 등)

    @Column(nullable = false, length = 255)
    private String roadAddress;

    @Column(length = 255)
    private String lotAddress;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(length = 100)
    private String operatingHours;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Builder
    private Shelter(String externalId, String name, ShelterType shelterType, String facilityType,
                    String roadAddress, String lotAddress, Double latitude, Double longitude,
                    Integer capacity, String operatingHours, String remark) {
        this.externalId = externalId;
        this.name = name;
        this.shelterType = shelterType;
        this.facilityType = facilityType;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacity = capacity;
        this.operatingHours = operatingHours;
        this.remark = remark;
    }

    public void update(String name, String facilityType, String roadAddress, String lotAddress,
                       Double latitude, Double longitude, Integer capacity, String operatingHours, String remark) {
        this.name = name;
        this.facilityType = facilityType;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacity = capacity;
        this.operatingHours = operatingHours;
        this.remark = remark;
    }
}
