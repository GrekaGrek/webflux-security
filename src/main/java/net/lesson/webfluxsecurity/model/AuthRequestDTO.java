package net.lesson.webfluxsecurity.model;

public record AuthRequestDTO(
        String username,
        String password
) {
}
