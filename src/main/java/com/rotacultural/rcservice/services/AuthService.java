package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.auth.LoginReq;
import com.rotacultural.rcservice.dtos.auth.LoginResp;
import com.rotacultural.rcservice.dtos.users.UserResp;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.enums.Role;
import com.rotacultural.rcservice.repositories.UsersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    public static final long TOKEN_EXPIRATION_SECONDS = 86400L;

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthService(
            UsersRepository usersRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder
    ) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResp login(LoginReq req) {
        String email = req.getEmail() != null ? req.getEmail().trim() : "";

        Users user = usersRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "E-mail ou senha inválidos"
                ));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "E-mail ou senha inválidos"
            );
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("rc-service")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(TOKEN_EXPIRATION_SECONDS))
                .subject(String.valueOf(user.getIdUser()))
                .claim("userId", user.getIdUser())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("role", List.of(user.getRole().name()))
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).getTokenValue();

        return new LoginResp(token, "Bearer", TOKEN_EXPIRATION_SECONDS, toUserResponse(user));
    }

    public UserResp me() {
        return toUserResponse(getCurrentUser());
    }

    public Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        Long userId = extractUserId(jwt);
        return usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário autenticado não encontrado"
                ));
    }

    public Optional<Users> getOptionalCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        try {
            Long userId = extractUserId(jwt);
            return usersRepository.findById(userId);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public boolean isAdmin() {
        return getOptionalCurrentUser()
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public void requireOwnerOrAdmin(Long resourceOwnerId) {
        Users currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getIdUser().equals(resourceOwnerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar ou modificar recursos de outro usuário"
            );
        }
    }

    public UserResp toUserResponse(Users user) {
        UserResp response = new UserResp();
        response.setId(user.getIdUser());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setCep(user.getCep());
        response.setRole(user.getRole());
        response.setNativeRequested(Boolean.TRUE.equals(user.getNativeRequested()));
        response.setBio(user.getBio());
        response.setDateCreate(user.getDateCreate());
        return response;
    }

    private Long extractUserId(Jwt jwt) {
        if (jwt.getSubject() != null) {
            try {
                return Long.valueOf(jwt.getSubject());
            } catch (NumberFormatException ignored) {
                // fallback para claim userId ou email
            }
        }
        Object claim = jwt.getClaim("userId");
        if (claim instanceof Number number) {
            return number.longValue();
        }
        String email = jwt.getClaimAsString("email");
        if (email != null) {
            return usersRepository.findByEmailIgnoreCase(email)
                    .map(Users::getIdUser)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido"));
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
    }
}
