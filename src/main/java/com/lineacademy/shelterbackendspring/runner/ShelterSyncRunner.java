package com.lineacademy.shelterbackendspring.runner;

import com.lineacademy.shelterbackendspring.service.ShelterSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShelterSyncRunner implements ApplicationRunner {

    private final ShelterSyncService shelterSyncService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("서버 구동 완료: 공공데이터 동기화 작업을 시작합니다.");
        shelterSyncService.syncAllShelters()
                .subscribe(); // Non-blocking 백그라운드 실행
    }
}
