package com.lineacademy.shelterbackendspring.controller;

import com.lineacademy.shelterbackendspring.dto.bookmark.response.BookmarkResponse;
import com.lineacademy.shelterbackendspring.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 내 북마크 전체 목록 조회
     */
    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getMyBookmarks(@AuthenticationPrincipal String email) {
        return bookmarkService.getMyBookmarks(email)
                .map(BookmarkResponse::from)
                .collectList()
                .map(list -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", list);
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * 내 북마크 쉼터 ID 목록만 조회 (간단 조회용)
     */
    @GetMapping("/ids")
    public Mono<ResponseEntity<Map<String, Object>>> getMyBookmarkIds(@AuthenticationPrincipal String email) {
        return bookmarkService.getMyBookmarkShelterIds(email)
                .map(ids -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", ids);
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * 북마크 등록
     */
    @PostMapping("/{shelterId}")
    public Mono<ResponseEntity<Map<String, Object>>> addBookmark(
            @AuthenticationPrincipal String email,
            @PathVariable Long shelterId) {

        return bookmarkService.addBookmark(email, shelterId)
                .map(bookmark -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", BookmarkResponse.from(bookmark));
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }

    /**
     * 북마크 삭제
     */
    @DeleteMapping("/{shelterId}")
    public Mono<ResponseEntity<Map<String, Object>>> removeBookmark(
            @AuthenticationPrincipal String email,
            @PathVariable Long shelterId) {

        return bookmarkService.removeBookmark(email, shelterId)
                .then(Mono.fromCallable(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "북마크가 삭제되었습니다.");
                    return ResponseEntity.ok(response);
                }))
                .onErrorResume(IllegalArgumentException.class, e -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }
}