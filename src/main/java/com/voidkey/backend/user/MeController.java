package com.voidkey.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * This is your Phase-1 "does the whole auth chain actually work" test endpoint:
 * signup -> get a token -> call GET /api/me with "Authorization: Bearer <token>" ->
 * should return your own name/email. No token, or a bad token -> 401/403, because
 * SecurityConfig marks anyRequest() as authenticated() except /api/auth/** and docs.
 *
 * @AuthenticationPrincipal pulls the User object that JwtAuthFilter placed into the
 * SecurityContext earlier in the chain — this is the payoff of that filter existing.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeController {

    @GetMapping("/me")
    public Map<String, String> me(@AuthenticationPrincipal User user) {
        return Map.of(
                "id", String.valueOf(user.getId()),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        );
    }
}
