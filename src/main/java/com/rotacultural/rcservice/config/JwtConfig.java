
package com.rotacultural.rcservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;

@Configuration
public class JwtConfig {

    public static final String DEV_DEFAULT_SECRET_BASE64 =
            "cm90YWN1bHR1cmFsLXNlY3JldC1rZXktMjU2LWJpdHMtbWluaW11bS1mb3ItaHMyNTY=";

    // Chave secreta compartilhada pelo encoder e decoder
    @Bean
    public SecretKey jwtSecretKey(
            @Value("${jwt.secret-base64:}") String secret,
            Environment environment
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET_BASE64 deve ser configurado");
        }

        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (isProd && DEV_DEFAULT_SECRET_BASE64.equals(secret.trim())) {
            throw new IllegalStateException(
                    "JWT_SECRET_BASE64 padrão de desenvolvimento não pode ser usado em produção"
            );
        }

        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "JWT_SECRET_BASE64 deve conter um Base64 válido", e
            );
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "A chave JWT deve possuir no mínimo 256 bits"
            );
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // Assina os tokens durante o login
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder
                .withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    // Valida os tokens recebidos nas requisições
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Valida expiração, emissão e emissor do token
        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer("rc-service")
        );

        return decoder;
    }
}
