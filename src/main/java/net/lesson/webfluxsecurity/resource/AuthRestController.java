package net.lesson.webfluxsecurity.resource;

import lombok.RequiredArgsConstructor;
import net.lesson.webfluxsecurity.mapper.UserMapper;
import net.lesson.webfluxsecurity.model.AuthRequestDTO;
import net.lesson.webfluxsecurity.model.AuthResponseDTO;
import net.lesson.webfluxsecurity.model.UserDTO;
import net.lesson.webfluxsecurity.security.CustomPrincipal;
import net.lesson.webfluxsecurity.security.SecurityService;
import net.lesson.webfluxsecurity.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
class AuthRestController {
    private final UserService userService;
    private final SecurityService service;
    private final UserMapper mapper;

    @PostMapping("/register")
    public Mono<UserDTO> register(@RequestBody UserDTO item) {
        return userService.registerUser(mapper.map(item))
                .map(mapper::map);
    }

    @PostMapping("/login")
    public Mono<AuthResponseDTO> login(@RequestBody AuthRequestDTO request) {
        return service.authenticate(request.username(), request.password())
                .flatMap(tokenDetails -> Mono.just(
                        AuthResponseDTO.builder()
                                .userId(tokenDetails.userId())
                                .token(tokenDetails.token())
                                .issuedAt(tokenDetails.issuedAt())
                                .expiresAt(tokenDetails.expiresAt())
                                .build()
                ));
    }

    @GetMapping("/info")
    public Mono<UserDTO> fetchUserInfo(Authentication auth) {
        CustomPrincipal principal = (CustomPrincipal) auth.getPrincipal();
        return userService.getUserById(principal.getId())
                .map(mapper::map);
    }
}
