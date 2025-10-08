package com.revquix.backend.auth.config;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: JwtConfig
 */

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.revquix.backend.auth.authentication.KeyProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JwtConfig {

    private final KeyProvider keyProvider;

    @Bean
    public JwtEncoder jwtEncoder() {
        PrivateKey privateKey = keyProvider.getPrivateKey();
        PublicKey publicKey = keyProvider.getPublicKey();
        JWK jwk = new RSAKey.Builder((RSAPublicKey) publicKey)
                .privateKey((RSAPrivateKey) privateKey)
                .build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        PublicKey publicKey = keyProvider.getPublicKey();
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) publicKey).build();
    }
}
