package com.lineacademy.shelterbackendspring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lineacademy.shelterbackendspring.config.properties.PublicApiProperties;
import com.lineacademy.shelterbackendspring.domain.entity.Shelter;
import com.lineacademy.shelterbackendspring.domain.enums.ShelterType;
import com.lineacademy.shelterbackendspring.repository.ShelterRepository;
import com.lineacademy.shelterbackendspring.util.CoordinateTransformUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShelterSyncService {

    private final WebClient seoulOpenApiWebClient;
    private final PublicApiProperties publicApiProperties;
    private final ShelterRepository shelterRepository;
    private final CoordinateTransformUtil coordinateTransformUtil;

    private static final int PAGE_SIZE = 1000;
    private final ObjectMapper objectMapper;

    /**
     * 4개 쉼터 API 전체 동기화 실행
     */
    public Mono<Void> syncAllShelters() {
        log.info("=== 서울시 쉼터 데이터 전체 동기화 시작 ===");
        return Flux.concat(
                        syncHeatShelters(),
                        syncColdShelters(),
                        syncClimateShelters(),
                        syncDustShelters()
                )
                .then()
                .doOnSuccess(v -> log.info("=== 서울시 쉼터 데이터 전체 동기화 완료 ==="))
                .doOnError(e -> log.error("쉼터 동기화 실패: {}", e.getMessage(), e));
    }

    // 1. 무더위 쉼터 (TbGtnHwcwP)
    private Mono<Void> syncHeatShelters() {
        String serviceName = "TbGtnHwcwP";
        return fetchAllPages(serviceName)
                .flatMap(root -> processShelterRows(root, serviceName, ShelterType.HEAT, this::parseHeatShelter))
                .then();
    }

    // 2. 한파 쉼터 (TbGtnCwP)
    private Mono<Void> syncColdShelters() {
        String serviceName = "TbGtnCwP";
        return fetchAllPages(serviceName)
                .flatMap(root -> processShelterRows(root, serviceName, ShelterType.COLD, this::parseColdShelter))
                .then();
    }

    // 3. 기후동행 쉼터 (tbClicomCnt) - TM 좌표 변환 적용
    private Mono<Void> syncClimateShelters() {
        String serviceName = "tbClicomCnt";
        return fetchAllPages(serviceName)
                .flatMap(root -> processShelterRows(root, serviceName, ShelterType.CLIMATE, this::parseClimateShelter))
                .then();
    }

    // 4. 미세먼지 쉼터 (shuntPlace) - TM 좌표 변환 적용
    private Mono<Void> syncDustShelters() {
        String serviceName = "shuntPlace";
        return fetchAllPages(serviceName)
                .flatMap(root -> processShelterRows(root, serviceName, ShelterType.DUST, this::parseDustShelter))
                .then();
    }

    /**
     * 전체 페이지 조회 파이프라인
     */
    private Flux<JsonNode> fetchAllPages(String serviceName) {
        return fetchPage(serviceName, 1, 1)
                .flatMapMany(initialNode -> {
                    JsonNode serviceNode = initialNode.path(serviceName);
                    int totalCount = serviceNode.path("list_total_count").asInt(0);

                    if (totalCount == 0) {
                        return Flux.empty();
                    }

                    List<int[]> pageRanges = new ArrayList<>();
                    for (int start = 1; start <= totalCount; start += PAGE_SIZE) {
                        int end = Math.min(start + PAGE_SIZE - 1, totalCount);
                        pageRanges.add(new int[]{start, end});
                    }

                    return Flux.fromIterable(pageRanges)
                            .concatMap(range -> fetchPage(serviceName, range[0], range[1]));
                });
    }

    /**
     * 문자열(String)로 응답 수신 후 ObjectMapper로 JsonNode 파싱
     */
    private Mono<JsonNode> fetchPage(String serviceName, int startIdx, int endIdx) {
        String url = String.format("%s/%s/json/%s/%d/%d/",
                publicApiProperties.getBaseUrl(),
                publicApiProperties.getKey(),
                serviceName,
                startIdx,
                endIdx
        );

        return seoulOpenApiWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(responseBody);
                        return Mono.just(jsonNode);
                    } catch (Exception e) {
                        log.error("[{}] JSON 파싱 오류: {}", serviceName, e.getMessage());
                        return Mono.empty();
                    }
                })
                .onErrorResume(e -> {
                    log.error("[{}] API 호출 실패 (범위: {}~{}): {}", serviceName, startIdx, endIdx, e.getMessage());
                    return Mono.empty();
                });
    }

    private Mono<Void> processShelterRows(JsonNode rootNode, String serviceName, ShelterType type, RowParser parser) {
        JsonNode rows = rootNode.path(serviceName).path("row");
        if (!rows.isArray()) return Mono.empty();

        return Flux.fromIterable(rows)
                .flatMap(row -> {
                    try {
                        Shelter shelter = parser.parse(row);
                        if (shelter == null || shelter.getLatitude() == null || shelter.getLongitude() == null) {
                            return Mono.empty();
                        }
                        return saveOrUpdateShelter(shelter);
                    } catch (Exception e) {
                        log.warn("[{}] 데이터 파싱 실패: {}", type, e.getMessage());
                        return Mono.empty();
                    }
                })
                .then();
    }

    @Transactional
    public Mono<Shelter> saveOrUpdateShelter(Shelter newShelter) {
        return Mono.fromCallable(() -> shelterRepository.findByExternalIdAndShelterType(newShelter.getExternalId(), newShelter.getShelterType())
                        .map(existing -> {
                            existing.update(
                                    newShelter.getName(),
                                    newShelter.getFacilityType(),
                                    newShelter.getRoadAddress(),
                                    newShelter.getLotAddress(),
                                    newShelter.getLatitude(),
                                    newShelter.getLongitude(),
                                    newShelter.getCapacity(),
                                    newShelter.getOperatingHours(),
                                    newShelter.getRemark()
                            );
                            return shelterRepository.save(existing);
                        })
                        .orElseGet(() -> shelterRepository.save(newShelter)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // === 각 API별 파싱 로직 ===

    private Shelter parseHeatShelter(JsonNode row) {
        String name = row.path("R_AREA_NM").asText();
        String areaCd = row.path("AREA_CD").asText();
        String externalId = areaCd + "_" + name;

        double lat = row.path("LAT").asDouble(0.0);
        double lng = row.path("LON").asDouble(0.0);
        if (lat == 0.0 || lng == 0.0) return null;

        String roadAddr = row.path("R_DETL_ADD").asText("");
        String lotAddr = row.path("LOTNO_ADDR").asText("");
        String hours = row.path("OPR_START_TIME").asText() + "~" + row.path("OPR_END_TIME").asText();

        return Shelter.builder()
                .externalId(externalId)
                .name(name)
                .shelterType(ShelterType.HEAT)
                .facilityType(row.path("FACILITY_TYPE1").asText())
                .roadAddress(StringUtils.hasText(roadAddr) ? roadAddr : lotAddr)
                .lotAddress(lotAddr)
                .latitude(lat)
                .longitude(lng)
                .operatingHours(hours)
                .remark(row.path("RMRK").asText())
                .build();
    }

    private Shelter parseColdShelter(JsonNode row) {
        String externalId = String.valueOf(row.path("SNO").asLong());
        String name = row.path("RESTAREA_NM").asText();

        double lat = row.path("LAT").asDouble(0.0);
        double lng = row.path("LOT").asDouble(0.0);
        if (lat == 0.0 || lng == 0.0) return null;

        String roadAddr = row.path("ROAD_NM_ADDR").asText("");
        String lotAddr = row.path("LOTNO_ADDR").asText("");

        return Shelter.builder()
                .externalId(externalId)
                .name(name)
                .shelterType(ShelterType.COLD)
                .facilityType(row.path("FACILITY_TYPE1").asText())
                .roadAddress(StringUtils.hasText(roadAddr) ? roadAddr : lotAddr)
                .lotAddress(lotAddr)
                .latitude(lat)
                .longitude(lng)
                .capacity(row.path("UTZTN_PSBLTY_NOPE").asInt(0))
                .remark(row.path("RMRK").asText())
                .build();
    }

    private Shelter parseClimateShelter(JsonNode row) {
        String externalId = String.valueOf(row.path("SN").asLong());
        String name = row.path("CNT_NM").asText();

        double x = row.path("MAP_COORD_X").asDouble(0.0);
        double y = row.path("MAP_COORD_Y").asDouble(0.0);
        if (x == 0.0 || y == 0.0) return null;

        double[] wgs84 = coordinateTransformUtil.transformTmToWgs84(x, y);

        return Shelter.builder()
                .externalId(externalId)
                .name(name)
                .shelterType(ShelterType.CLIMATE)
                .facilityType(row.path("CNT_GB").asText())
                .roadAddress(row.path("ROAD_NM_ADDR").asText())
                .latitude(wgs84[0])
                .longitude(wgs84[1])
                .operatingHours(row.path("OPER_HR").asText())
                .build();
    }

    private Shelter parseDustShelter(JsonNode row) {
        String externalId = String.valueOf(row.path("SNO").asLong());
        String name = row.path("FCLT_NM").asText();

        double x = row.path("XCRD").asDouble(0.0);
        double y = row.path("YCRD").asDouble(0.0);
        if (x == 0.0 || y == 0.0) return null;

        double[] wgs84 = coordinateTransformUtil.transformTmToWgs84(x, y);

        return Shelter.builder()
                .externalId(externalId)
                .name(name)
                .shelterType(ShelterType.DUST)
                .facilityType(row.path("FCLT_TYPE").asText())
                .roadAddress(row.path("ADDR").asText())
                .latitude(wgs84[0])
                .longitude(wgs84[1])
                .capacity(row.path("UTZTN_PSBLTY_NOPE").asInt(0))
                .operatingHours(row.path("WD_UTZTN_HRM").asText())
                .remark(row.path("RMRK").asText())
                .build();
    }

    @FunctionalInterface
    private interface RowParser {
        Shelter parse(JsonNode row);
    }
}