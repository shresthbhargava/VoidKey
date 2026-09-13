package com.voidkey.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Why this exists separately from SecurityConfig: your frontend (built in Antigravity)
 * will run on a different origin (different port locally, different domain in prod)
 * than this API. Without CORS rules, the browser blocks the frontend's fetch() calls
 * before they even reach Spring — this is a browser-enforced restriction, not
 * something Postman/curl will ever show you, which is why "it works in Postman but
 * not the browser" is almost always a missing-CORS-config problem.
 *
 * TODO: once you know your deployed frontend's real domain, replace the localhost
 * entries below with it. Never use allowedOrigins("*") together with allowCredentials(true)
 * — browsers reject that combination anyway, and it would defeat the purpose of CORS.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
