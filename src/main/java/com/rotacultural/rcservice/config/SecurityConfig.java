package com.rotacultural.rcservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000}")
    private String allowedOrigins;

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
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Rotas específicas de locais com restrição antes dos wildcards públicos
                        .requestMatchers(HttpMethod.GET, "/places/my").hasRole("NATIVO")
                        .requestMatchers(HttpMethod.GET, "/places/admin").hasRole("ADMIN")

                        // Consultas e ações administrativas globais
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
                        .requestMatchers(HttpMethod.PATCH, "/user/*/role").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/user/*/role").hasRole("ADMIN")

                        // Rotas públicas - VISITANTE
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login",
                                "/user",
                                "/recommendations/preview"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/health",
                                "/categories", "/categories/*",
                                "/places", "/places/*", "/places/*/images",
                                "/feedbacks", "/feedbacks/*", "/feedbacks/place/*",
                                "/uploads/**"
                        ).permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Autenticação - usuário logado
                        .requestMatchers(HttpMethod.GET, "/auth/me").hasRole("USUARIO")

                        // ADMIN - Categorias e aprovação de locais
                        .requestMatchers("/categories", "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/places/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/places/*/status").hasRole("ADMIN")

                        // NATIVO (e ADMIN pela hierarquia) - Cadastro, edição e imagens dos próprios locais
                        .requestMatchers(HttpMethod.POST, "/places", "/places/*/images").hasRole("NATIVO")
                        .requestMatchers(HttpMethod.PUT, "/places/*").hasRole("NATIVO")
                        .requestMatchers(HttpMethod.DELETE, "/places/*", "/places/*/images/*").hasRole("NATIVO")

                        // USUARIO (e NATIVO/ADMIN pela hierarquia) - Recursos pessoais (com checagem de propriedade no service)
                        .requestMatchers("/feedbacks", "/feedbacks/**").hasRole("USUARIO")
                        .requestMatchers(
                                "/user/**",
                                "/trip", "/trip/**",
                                "/favorites", "/favorites/**",
                                "/preferences", "/preferences/**",
                                "/itineraries", "/itineraries/**",
                                "/recommendations", "/recommendations/**"
                        ).hasRole("USUARIO")

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
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("role");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
