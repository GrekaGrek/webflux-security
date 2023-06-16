package net.lesson.webfluxsecurity.security;

import lombok.Builder;

import java.time.LocalDate;

@Builder(toBuilder = true)
public record TokenDetails (
    Long userId,
    String token,
    LocalDate issuedAt,
    LocalDate expiresAt
) {
}
