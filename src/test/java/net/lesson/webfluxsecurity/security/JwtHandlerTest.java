package net.lesson.webfluxsecurity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtHandlerTest {
    private static final String SECRET = "S13e8aENggaMbb_fAkl-nJL4AEVBX43g";

    @InjectMocks
    private JwtHandler jwtHandler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtHandler, "secret", SECRET);
    }

    @Test
    void checkTokenIsValidAndNotExpired() {
        String token = Jwts.builder()
                .setIssuedAt(Date.valueOf(LocalDate.of(2023, 5, 20)))
                .setId(UUID.randomUUID().toString())
                .setExpiration(Date.valueOf(LocalDate.of(2023, 6, 30)))
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(SECRET.getBytes()))
                .compact();
        Claims claims = Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(SECRET.getBytes()))
                .parseClaimsJws(token)
                .getBody();

        var actualResult = jwtHandler.check(token);

        StepVerifier
                .create(actualResult)
                .consumeNextWith(verificationResult -> {
                    new JwtHandler.VerificationResult(claims, token);
                })
                .verifyComplete();
    }

    @Test
    void checkTokenIsExpired() {
        String token = Jwts.builder()
                .setIssuedAt(Date.valueOf(LocalDate.of(2023, 5, 30)))
                .setId(UUID.randomUUID().toString())
                .setExpiration(Date.valueOf(LocalDate.of(2023, 5, 31)))
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(SECRET.getBytes()))
                .compact();

        assertThatThrownBy(() -> {
            jwtHandler.check(token);
        })
                .isInstanceOf(ExpiredJwtException.class)
                .hasMessageContaining("JWT expired at ");
    }
}