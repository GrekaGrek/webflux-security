package net.lesson.webfluxsecurity.service;

import net.lesson.webfluxsecurity.domain.UserEntity;
import net.lesson.webfluxsecurity.enums.UserRole;
import net.lesson.webfluxsecurity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private final String PASSWORD = "!2wdd";
    @Mock
    private PasswordEncoder mockPasswordEncoder;
    @Mock
    private UserRepository mockRepository;
    @Mock
    private Clock clock;

    @InjectMocks
    private UserService service;

    private static final ZonedDateTime NOW = ZonedDateTime.of(
            2023, 5, 14, 12, 40, 24, 0,
            ZoneId.of("Europe/Prague"));

    @Test
    void registerUserOk() {
        var entity = createEntity();

        given(mockPasswordEncoder.encode(any(CharSequence.class))).willReturn(PASSWORD);
        given(clock.getZone()).willReturn(NOW.getZone());
        given(clock.instant()).willReturn(NOW.toInstant());
        given(mockRepository.save(entity)).willReturn(Mono.just(entity));

        var actualResult = service.registerUser(entity);

        StepVerifier
                .create(actualResult)
                .expectNextMatches(userCreated -> userCreated.equals(entity))
                .verifyComplete();
    }

    @Test
    void getUserById() {
        var entity = createEntity();
        given(mockRepository.findById(entity.getId())).willReturn(Mono.just(entity));

        var actualResult = service.getUserById(entity.getId());

        StepVerifier
                .create(actualResult)
                .expectNextMatches(userCreated -> userCreated.equals(entity))
                .verifyComplete();
    }

    @Test
    void getUserByIdNotFound() {
        var entity = createEntity();
        given(mockRepository.findById(entity.getId())).willReturn(Mono.empty());

        var actualResult = service.getUserById(entity.getId());

        StepVerifier
                .create(actualResult)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getUserByName() {
        var entity = createEntity();
        given(mockRepository.findByUsername(entity.getUsername())).willReturn(Mono.just(entity));

        var actualResult = service.getUserByName(entity.getUsername());

        StepVerifier
                .create(actualResult)
                .expectNextMatches(userCreated -> userCreated.equals(entity))
                .verifyComplete();
    }

    @Test
    void getUserByNameNotFound() {
        var username = "user";
        given(mockRepository.findByUsername(username)).willReturn(Mono.empty());

        var actualResult = service.getUserByName(username);

        StepVerifier
                .create(actualResult)
                .expectNextCount(0)
                .verifyComplete();
    }

    private UserEntity createEntity() {
        String firstName = "Anko";
        String lastName = "Puben";
        String username = "Anbi";
        return UserEntity.builder()
                .id(1L)
                .username(username)
                .role(UserRole.USER)
                .password(PASSWORD)
                .firstName(firstName)
                .lastName(lastName)
                .enabled(true)
                .createdAt(LocalDateTime.of(2023, 5, 14, 12, 40, 24))
                .updatedAt(LocalDateTime.of(2023, 5, 14, 12, 40, 24))
                .build();
    }
}