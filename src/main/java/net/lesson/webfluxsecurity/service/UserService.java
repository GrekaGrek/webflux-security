package net.lesson.webfluxsecurity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lesson.webfluxsecurity.domain.UserEntity;
import net.lesson.webfluxsecurity.enums.UserRole;
import net.lesson.webfluxsecurity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final Clock clock;
    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public Mono<UserEntity> registerUser(UserEntity entity) {
        return repository.save(
                entity.toBuilder()
                        .password(encoder.encode(entity.getPassword()))
                        .role(UserRole.USER)
                        .enabled(true)
                        .createdAt(LocalDateTime.now(clock))
                        .updatedAt(LocalDateTime.now(clock))
                        .build()
        ).doOnSuccess(user -> {
            log.info("IN register - user: {} created", user);
        });
    }

    public Mono<UserEntity> getUserById(Long id) {
        return repository.findById(id);
    }

    public Mono<UserEntity> getUserByName(String name) {
        return repository.findByUsername(name);
    }
}
