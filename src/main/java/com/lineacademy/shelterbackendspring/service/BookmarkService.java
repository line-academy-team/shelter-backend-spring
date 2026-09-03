package com.lineacademy.shelterbackendspring.service;

import com.lineacademy.shelterbackendspring.domain.entity.Bookmark;
import com.lineacademy.shelterbackendspring.domain.entity.Shelter;
import com.lineacademy.shelterbackendspring.domain.entity.User;
import com.lineacademy.shelterbackendspring.repository.BookmarkRepository;
import com.lineacademy.shelterbackendspring.repository.ShelterRepository;
import com.lineacademy.shelterbackendspring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final ShelterRepository shelterRepository;

    /**
     * 내 북마크 목록 조회
     */
    @Transactional(readOnly = true)
    public Flux<Bookmark> getMyBookmarks(String email) {
        return Mono.fromCallable(() -> userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다.")))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(user -> Mono.fromCallable(() -> bookmarkRepository.findAllByUserIdWithShelter(user.getId()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMapMany(Flux::fromIterable));
    }

    /**
     * 내 북마크 쉼터 ID 목록만 조회 (지도 마커 및 모달에서 즐겨찾기 여부 확인용)
     */
    @Transactional(readOnly = true)
    public Mono<List<Long>> getMyBookmarkShelterIds(String email) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            return bookmarkRepository.findAllByUserIdWithShelter(user.getId())
                    .stream()
                    .map(b -> b.getShelter().getId())
                    .toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 북마크 추가
     */
    @Transactional
    public Mono<Bookmark> addBookmark(String email, Long shelterId) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            Shelter shelter = shelterRepository.findById(shelterId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쉼터입니다."));

            if (bookmarkRepository.existsByUserIdAndShelterId(user.getId(), shelterId)) {
                throw new IllegalArgumentException("이미 북마크에 등록된 쉼터입니다.");
            }

            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .shelter(shelter)
                    .build();

            return bookmarkRepository.save(bookmark);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 북마크 삭제 (쉼터 ID 기준)
     */
    @Transactional
    public Mono<Void> removeBookmark(String email, Long shelterId) {
        return Mono.fromRunnable(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

            Bookmark bookmark = bookmarkRepository.findByUserIdAndShelterId(user.getId(), shelterId)
                    .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 북마크입니다."));

            bookmarkRepository.delete(bookmark);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
     @Transactional(readOnly = true)
        public Mono<Boolean> isBookmarked(String email, Long shelterId) {
            return Mono.fromCallable(() -> {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
                return bookmarkRepository.existsByUserIdAndShelterId(user.getId(), shelterId);
            }).subscribeOn(Schedulers.boundedElastic());
        }
}