package com.lineacademy.shelterbackendspring.service;

import com.lineacademy.shelterbackendspring.config.JwtProvider;
import com.lineacademy.shelterbackendspring.domain.entity.User;
import com.lineacademy.shelterbackendspring.domain.enums.UserRole;
import com.lineacademy.shelterbackendspring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 로그인 기능
    @Transactional(readOnly = true)
    public Mono<Tuple2<String, User>> login(String email, String rawPassword) {
        return Mono.fromCallable(() -> userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalAccessException("존재하지 않는 사용자입니다.")))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                        return Mono.error(new IllegalAccessException("비밀번호가 일치하지 않습니다."));
                    }

                    String token = jwtProvider.createToken(user.getEmail(), user.getRole());

                    return Mono.just(Tuples.of(token, user));
                });
    }

    // 회원가입 기능
    @Transactional
    public Mono<User> signup(String email, String rawPassword, String nickname) {
        return Mono.fromCallable(() -> userRepository.existsByEmail(email))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("이미 사용 중인 이메일입니다."));
                    }

                    User newUser = User.builder()
                            .email(email)
                            .password(passwordEncoder.encode(rawPassword))
                            .nickname(nickname)
                            .role(UserRole.USER)
                            .build();

                    return Mono.fromCallable(() -> userRepository.save(newUser))
                            .subscribeOn(Schedulers.boundedElastic());
                });
    }

    @Transactional(readOnly = true)
    public Mono<User> getCurrentUser(String email) {
        return Mono.fromCallable(() -> userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다.")))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
