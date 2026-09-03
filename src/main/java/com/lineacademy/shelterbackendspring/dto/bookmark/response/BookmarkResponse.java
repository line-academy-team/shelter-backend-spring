package com.lineacademy.shelterbackendspring.dto.bookmark.response;

import com.lineacademy.shelterbackendspring.domain.entity.Bookmark;
import com.lineacademy.shelterbackendspring.dto.shelter.response.ShelterResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkResponse {
    private Long bookmarkId;
    private ShelterResponse shelter;

    public static BookmarkResponse from(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .bookmarkId(bookmark.getId())
                .shelter(ShelterResponse.from(bookmark.getShelter()))
                .build();
    }
}