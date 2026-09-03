package com.lineacademy.shelterbackendspring.repository;

import com.lineacademy.shelterbackendspring.domain.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // N+1 방지를 위한 JOIN FETCH 쿼리
    @Query("SELECT b FROM Bookmark b JOIN FETCH b.shelter WHERE b.user.id = :userId")
    List<Bookmark> findAllByUserIdWithShelter(@Param("userId") Long userId);

    Optional<Bookmark> findByUserIdAndShelterId(Long userId, Long shelterId);

    boolean existsByUserIdAndShelterId(Long userId, Long shelterId);
}