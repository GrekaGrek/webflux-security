package net.lesson.webfluxsecurity.security;

import net.lesson.webfluxsecurity.domain.UserEntity;
import net.lesson.webfluxsecurity.enums.UserRole;
import net.lesson.webfluxsecurity.exception.UnauthorizedException;
import net.lesson.webfluxsecurity.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthManagerTest {

    @Mock
    private UserService mockUserService;

    @InjectMocks
    private AuthManager authManager;

    @Test
    void authenticateOK() {
        var entity = createEntity();
        var principal = new CustomPrincipal(1L, "name");
        given(mockUserService.getUserById(principal.getId())).willReturn(Mono.just(entity));

        var actualResult = authManager.authenticate(new UsernamePasswordAuthenticationToken(principal, null, new ArrayList<>()));

        StepVerifier
                .create(actualResult)
                .consumeNextWith(authentication -> {
                    assertTrue(authentication.isAuthenticated());
                })
                .verifyComplete();
    }

    @Test
    void authenticateThrowsException() {
        var principal = new CustomPrincipal(10L, "test");
        given(mockUserService.getUserById(principal.getId())).willReturn(Mono.empty());

        var actualResult = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(principal, null, new ArrayList<>())
        );

        StepVerifier
                .create(actualResult)
                .expectErrorMatches(throwable -> throwable instanceof UnauthorizedException)
                .verify();
    }

    private UserEntity createEntity() {
        var firstName = "Anko";
        var lastName = "Puben";
        var USERNAME = "rebasok";
        var PASSWORD = "#4!wqseR";
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