
package com.rotacultural.rcservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Hierarquia cumulativa de permissões
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("NATIVO")
                .role("NATIVO").implies("USUARIO")
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtConverter
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())

                .authorizeHttpRequests(auth -> auth

                        // PÚBLICO - VISITANTE
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login",
                                "/user"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/categories", "/categories/*",
                                "/places", "/places/*",
                                "/feedbacks", "/feedbacks/*"
                        ).permitAll()

                        // ADMIN - Documentação
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).hasRole("ADMIN")

                        // ADMIN - Consultas administrativas globais
                        .requestMatchers(
                                HttpMethod.GET,
                                "/user",
                                "/user/trip",
                                "/user/cep/**",
                                "/trip",
                                "/favorites",
                                "/preferences",
                                "/itineraries"
                        ).hasRole("ADMIN")

                        // ADMIN - Gerenciamento de categorias
                        .requestMatchers(
                                "/categories", "/categories/**"
                        ).hasRole("ADMIN")

                        // NATIVO - Cadastro de locais
                        .requestMatchers(
                                HttpMethod.POST, "/places"
                        ).hasRole("NATIVO")

                        // ADMIN - Edição, aprovação e exclusão
                        .requestMatchers(
                                "/places", "/places/**"
                        ).hasRole("ADMIN")

                        // USUARIO - Criar avaliações
                        .requestMatchers(
                                HttpMethod.POST, "/feedbacks"
                        ).hasRole("USUARIO")

                        // ADMIN - Moderação de avaliações
                        .requestMatchers(
                                "/feedbacks", "/feedbacks/**"
                        ).hasRole("ADMIN")

                        // USUARIO - Recursos pessoais
                        // Exigem também validação de propriedade
                        .requestMatchers(
                                "/user/**",
                                "/trip", "/trip/**",
                                "/favorites", "/favorites/**",
                                "/preferences", "/preferences/**",
                                "/itineraries", "/itineraries/**"
                        ).hasRole("USUARIO")

                        // Bloqueia rotas não configuradas
                        .anyRequest().denyAll()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtConverter)
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("role");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
