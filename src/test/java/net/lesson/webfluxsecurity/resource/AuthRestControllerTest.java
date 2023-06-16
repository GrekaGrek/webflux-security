package net.lesson.webfluxsecurity.resource;

import net.lesson.webfluxsecurity.domain.UserEntity;
import net.lesson.webfluxsecurity.enums.UserRole;
import net.lesson.webfluxsecurity.errorhandling.AppErrorAttributes;
import net.lesson.webfluxsecurity.exception.ApiException;
import net.lesson.webfluxsecurity.exception.UnauthorizedException;
import net.lesson.webfluxsecurity.mapper.UserMapper;
import net.lesson.webfluxsecurity.model.AuthRequestDTO;
import net.lesson.webfluxsecurity.model.AuthResponseDTO;
import net.lesson.webfluxsecurity.model.UserDTO;
import net.lesson.webfluxsecurity.security.CustomPrincipal;
import net.lesson.webfluxsecurity.security.SecurityService;
import net.lesson.webfluxsecurity.security.TokenDetails;
import net.lesson.webfluxsecurity.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;


@WebFluxTest(controllers = AuthRestController.class)
class AuthRestControllerTest {
    private static final String TEST_URI = "/api/v1/auth/";
    private final String USERNAME = "Anbi";
    private final String PASSWORD = "!2wdd";
    private final String FIRST_NAME = "Anko";
    private final String LAST_NAME = "Puben";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AppErrorAttributes attributes;
    @MockBean
    private UserService mockUserService;
    @MockBean
    private SecurityService mockSecurityService;
    @MockBean
    private UserMapper mockUserMapper;

    @Test
    @WithMockUser
    void registerUserAndReturnStatusOk() {
        var user = requestDTO();
        var entity = createEntity();
        var item = responseItem();

        given(mockUserService.registerUser(mockUserMapper.map(user))).willReturn(Mono.just(entity));
        given(mockUserMapper.map(user)).willReturn(entity);
        given(mockUserMapper.map(entity)).willReturn(item);

        webTestClient
                .mutateWith(csrf())
                .post()
                .uri(TEST_URI + "register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(user))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class);
    }

    @Test
    void registerUserUnauthorized() {
        var user = requestDTO();

        given(mockUserService.registerUser(mockUserMapper.map(user))).willThrow(new UnauthorizedException("UNAUTHORIZED"));

        webTestClient
                .mutateWith(csrf())
                .post()
                .uri(TEST_URI + "register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(user))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody().isEmpty();
    }
    @Test
    @WithMockUser
    void registerUserIsForbidden() {
        var user = requestDTO();

        given(mockUserService.registerUser(mockUserMapper.map(user))).willThrow(new ApiException("FORBIDDEN", "403"));

        webTestClient
                .post()
                .uri(TEST_URI + "register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(user))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser
    void loginWithOkStatus() {
        var request = authRequestDTO();
        var tokenDetails = getDetails();

        given(mockSecurityService.authenticate(request.username(), request.password())).willReturn(Mono.just(tokenDetails));

        webTestClient
                .mutateWith(csrf())
                .post()
                .uri(TEST_URI + "login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDTO.class);
    }

    @Test
    @WithMockUser
    void loginFailed() {
        var username = "papaRouagh";
        var password = "!gkgflgd@3332";

        given(mockSecurityService.authenticate(username, password)).willThrow(new RuntimeException("BAD REQUEST"));

        webTestClient
                .mutateWith(csrf())
                .post()
                .uri(TEST_URI + "login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(username))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(AuthResponseDTO.class);
    }

    @Test
    void successfullyFetchUserInfo() {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(UserRole.USER.name()));
        var auth = new UsernamePasswordAuthenticationToken(new CustomPrincipal(6L, USERNAME), null, authorities);
        var principal = (CustomPrincipal) auth.getPrincipal();

        var userEntity = createEntity();
        var item = responseItem();

        given(mockUserService.getUserById(principal.getId())).willReturn(Mono.just(userEntity));
        given(mockUserMapper.map(userEntity)).willReturn(item);

        webTestClient
                .mutateWith(csrf())
                .mutateWith(mockAuthentication(auth))
                .get()
                .uri(TEST_URI + "info")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class);
    }

    @Test
    @WithMockUser
    void failedWith500ToFetchUserInfo() {
        given(mockUserService.getUserById(anyLong())).willThrow(new ClassCastException());

        webTestClient
                .mutateWith(csrf())
                .get()
                .uri(TEST_URI + "info")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    private UserEntity createEntity() {
        return UserEntity.builder()
                .id(1L)
                .username(USERNAME)
                .role(UserRole.USER)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .enabled(true)
                .createdAt(LocalDateTime.of(2023, 2, 21, 3, 32))
                .updatedAt(LocalDateTime.of(2023, 2, 21, 3, 34))
                .build();
    }

    private UserDTO requestDTO() {
        return UserDTO.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();
    }

    private UserDTO responseItem() {
        return UserDTO.builder()
                .id(16L)
                .username(USERNAME)
                .role(UserRole.USER)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .enabled(true)
                .createdAt(LocalDateTime.of(2023, 2, 21, 3, 32))
                .updatedAt(LocalDateTime.of(2023, 2, 21, 3, 32))
                .build();
    }

    private AuthRequestDTO authRequestDTO() {
        return new AuthRequestDTO(USERNAME, PASSWORD);
    }

    private TokenDetails getDetails() {
        return TokenDetails.builder()
                .userId(16L)
                .token("")
                .issuedAt(LocalDate.of(2023, 3, 2))
                .expiresAt(LocalDate.of(2023, 5, 30))
                .build();
    }
}