package com.revquix.backend.auth.config;

/*
  Developer: Rohit Parihar
  Project: revquix-backend
  GitHub: github.com/rohit-zip
  File: CorsConfiguration
 */

import com.revquix.backend.auth.properties.AuthenticationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CorsConfig {

    private final AuthenticationProperties authenticationProperties;

    private static final List<String> ALLOWED_METHODS = List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
    );

    private static final List<String> ALLOWED_HEADERS = List.of(
            "Authorization", "Content-Type", "Accept", "Origin",
            "Access-Control-Request-Method", "Access-Control-Request-Headers",
            "X-Requested-With", "X-Auth-Token", "X-Client-Version",
            "User-Agent", "Cache-Control", "Pragma", "Expires"
    );

    private static final List<String> EXPOSED_HEADERS = List.of(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials",
            "Authorization", "Content-Disposition", "Content-Length", "X-Total-Count"
    );

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("CorsConfig::corsConfigurationSource");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(authenticationProperties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(ALLOWED_METHODS);
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        configuration.setExposedHeaders(EXPOSED_HEADERS);
        configuration.setAllowCredentials(authenticationProperties.getCors().isAllowCredentials());
        configuration.setMaxAge(authenticationProperties.getCors().getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
