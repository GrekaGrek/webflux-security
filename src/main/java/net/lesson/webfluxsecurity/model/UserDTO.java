package net.lesson.webfluxsecurity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import net.lesson.webfluxsecurity.enums.UserRole;

import java.time.LocalDateTime;

@Builder
//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserDTO(
        Long id,
        String username,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,
        UserRole role,
        String firstName,
        String lastName,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
