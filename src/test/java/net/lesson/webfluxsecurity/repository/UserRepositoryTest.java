package net.lesson.webfluxsecurity.repository;

import net.lesson.webfluxsecurity.domain.UserEntity;
import net.lesson.webfluxsecurity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;


@DataR2dbcTest
class UserRepositoryTest {
    private static final String USER_NAME = "Olamsha";
    private static final String LAST_NAME = "Dulkem";
    private static final String FIRST_NAME = "Abinshak";
    private static final String PASSWORD = "!124kfrQ";
    @Autowired
    private UserRepository repository;

    @Test
    void successfullyFindUserByUsername() {
        String username = "Abin";
        var user = new UserEntity(1L, username, PASSWORD, UserRole.USER, FIRST_NAME,
                LAST_NAME, true, LocalDateTime.now(), LocalDateTime.now());
        repository.save(user).subscribe();

        StepVerifier
                .create(repository.findByUsername(user.getUsername()))
                .expectNextMatches(userCreated -> userCreated.getUsername().equals(username))
                .verifyComplete();
    }

    @Test
    void userByUsernameNotFound() {
        String username = "Okonsh";
        var user = new UserEntity(1L, username, PASSWORD, UserRole.USER, FIRST_NAME,
                LAST_NAME, true, LocalDateTime.now(), LocalDateTime.now());
        repository.save(user).subscribe();

        StepVerifier
                .create(repository.findByUsername(USER_NAME))
                .expectNextCount(0)
                .verifyComplete();
    }
}