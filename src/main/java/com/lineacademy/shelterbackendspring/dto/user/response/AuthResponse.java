package com.lineacademy.shelterbackendspring.dto.user.response;

import com.lineacademy.shelterbackendspring.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private Long id;
    private String email;
    private String nickname;

    public static AuthResponse from(User user) {
        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
