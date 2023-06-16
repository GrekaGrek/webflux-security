package net.lesson.webfluxsecurity.security;

import net.lesson.webfluxsecurity.domain.UserEntity;
import net.lesson.webfluxsecurity.enums.UserRole;
import net.lesson.webfluxsecurity.exception.AuthException;
import net.lesson.webfluxsecurity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {
    private static final String USERNAME = "test";
    private static final String PASSWORD = "2@test!";
    private static final String SECRET = "ENggaMbb_fAkl";
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.";

    @Mock
    private PasswordEncoder mockPasswordEncoder;
    @Mock
    private UserService mockUserService;
    @InjectMocks
    private SecurityService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "secret", SECRET);
    }

    @Test
    void authenticateOk() {
        var tokenDetails = createTokenDetails();
        var entity = createEntity();

        given(mockUserService.getUserByName(USERNAME)).willReturn(Mono.just(entity));
        given(mockPasswordEncoder.matches(PASSWORD, entity.getPassword())).willReturn(true);

        var actualResult = service.authenticate(USERNAME, PASSWORD);

        StepVerifier
                .create(actualResult)
                .consumeNextWith(details -> {
                    assertEquals("User is enabled", tokenDetails.userId(), details.userId());
                    assertEquals("User token is ok", !tokenDetails.token().isEmpty(), !details.token().isEmpty());
                })
                .verifyComplete();
    }
    @Test
    void authenticateFailedAccount() {
        var entity = createEntity();

        given(mockUserService.getUserByName(USERNAME)).willReturn(Mono.just(entity));

        var actualResult = service.authenticate(USERNAME, PASSWORD);

        StepVerifier
                .create(actualResult)
                .expectErrorMatches(throwable -> throwable instanceof AuthException)
                .verify();
    }

    private TokenDetails createTokenDetails() {
        return TokenDetails.builder()
                .userId(1L)
                .token(TOKEN)
                .issuedAt(LocalDate.of(2023, 5, 31))
                .expiresAt(LocalDate.of(2023, 6, 1))
                .build();
    }

    private UserEntity createEntity() {
        var firstName = "Anko";
        var lastName = "Puben";
        return UserEntity.builder()
                .id(1L)
                .username(USERNAME)
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