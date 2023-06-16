package net.lesson.webfluxsecurity.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import net.lesson.webfluxsecurity.domain.UserEntity;
import net.lesson.webfluxsecurity.exception.AuthException;
import net.lesson.webfluxsecurity.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityService {
    private final UserService userService;
    private final PasswordEncoder encoder;
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Integer expiration;
    @Value("${jwt.issuer}")
    private String issuer;

    private TokenDetails generateToken(UserEntity user) {
        Map<String, Object> claims = new HashMap<>() {{
            put("role", user.getRole());
            put("username", user.getUsername());
        }};
        return generateToken(claims, user.getId().toString());
    }

    private TokenDetails generateToken(Map<String, Object> claims, String subject) {
        LocalDate expirationDate = LocalDate.now().plusDays(1);

        return generateToken(expirationDate, claims, subject);
    }

    private TokenDetails generateToken(LocalDate expirationDate, Map<String, Object> claims, String subject) {
        LocalDate createdDate = LocalDate.now();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setIssuedAt(Date.valueOf(createdDate))
                .setId(UUID.randomUUID().toString())
                .setExpiration(Date.valueOf(expirationDate))
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secret.getBytes()))
                .compact();

        return TokenDetails.builder()
                .token(token)
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .build();
    }

    public Mono<TokenDetails> authenticate(String username, String password) {
        return userService.getUserByName(username)
                .flatMap(user -> {
                    if (!user.isEnabled() || !encoder.matches(password, user.getPassword())) {
                        return Mono.error(new AuthException(
                                "Account disabled or Invalid password", "USER_ACCOUNT_DISABLED or INVALID_PASSWORD")
                        );
                    }
                    return Mono.just(generateToken(user).toBuilder()
                            .userId(user.getId())
                            .build());
                })
                .switchIfEmpty(Mono.error(new AuthException("Invalid username", "INVALID_USERNAME")));
    }
}
