package net.lesson.webfluxsecurity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import net.lesson.webfluxsecurity.exception.UnauthorizedException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;

public class JwtHandler {
    private final String secret;

    public JwtHandler(String secret) {
        this.secret = secret;
    }

    public Mono<VerificationResult> check(String accessToken) {
        return Mono.just(verify(accessToken))
                .onErrorResume(ex -> Mono.error(new UnauthorizedException(ex.getMessage())));
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
                .parseClaimsJws(token)
                .getBody();
    }

    private VerificationResult verify(String token) {
        Claims claims = getClaimsFromToken(token);
        final LocalDate expirationDate = claims.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (expirationDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Token expired");
        }
        return new VerificationResult(claims, token);
    }

    public static class VerificationResult {
        public Claims claims;
        public String token;

        public VerificationResult(Claims claims, String token) {
            this.claims = claims;
            this.token = token;
        }
    }
}
