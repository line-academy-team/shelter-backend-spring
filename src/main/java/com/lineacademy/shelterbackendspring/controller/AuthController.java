package com.lineacademy.shelterbackendspring.controller;

import com.lineacademy.shelterbackendspring.dto.user.request.LoginRequest;
import com.lineacademy.shelterbackendspring.dto.user.request.SignUpRequest;
import com.lineacademy.shelterbackendspring.dto.user.response.AuthResponse;
import com.lineacademy.shelterbackendspring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService authService;

    // 회원가입 API
    @PostMapping("/signup")
    public Mono<ResponseEntity<Map<String, Object>>> signup(
            @Valid @RequestBody SignUpRequest request
    ) {
        return authService
                .signup(
                        request.getEmail(),
                        request.getPassword(),
                        request.getNickname()
                )
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", AuthResponse.from(user));

                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(response);
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", e.getMessage());

                    return Mono.just(
                            ResponseEntity
                                    .badRequest()
                                    .body(errorResponse)
                    );
                });
    }

    // 로그인 API
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService
                .login(
                        request.getEmail(),
                        request.getPassword()
                )
                .map(tuple -> {
                    String token = tuple.getT1();
                    AuthResponse authResponse =
                            AuthResponse.from(tuple.getT2());

                    Map<String, Object> data = new HashMap<>();
                    data.put("token", token);
                    data.put("user", authResponse);

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", data);

                    return ResponseEntity
                            .ok()
                            .body(response);
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", e.getMessage());

                    return Mono.just(
                            ResponseEntity
                                    .status(HttpStatus.UNAUTHORIZED)
                                    .body(errorResponse)
                    );
                });
    }
}