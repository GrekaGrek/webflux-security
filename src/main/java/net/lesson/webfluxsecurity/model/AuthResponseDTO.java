package net.lesson.webfluxsecurity.model;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AuthResponseDTO(
        Long userId,
        String token,
        LocalDate issuedAt,
        LocalDate expiresAt
) {
}
