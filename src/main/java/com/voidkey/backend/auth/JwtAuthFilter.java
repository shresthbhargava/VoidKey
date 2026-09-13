package com.voidkey.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs on EVERY request, exactly once (that's what OncePerRequestFilter guarantees —
 * important because filter chains can otherwise apply a filter twice on internal
 * forwards/redirects).
 *
 * The job here is narrow and worth understanding precisely:
 *  1. Look for an "Authorization: Bearer <token>" header.
 *  2. If present and valid, tell Spring Security "this request is authenticated as X"
 *     by populating the SecurityContext.
 *  3. If absent or invalid, do nothing here — just pass the request along. The actual
 *     "is this endpoint allowed for anonymous users?" decision happens later, in
 *     SecurityConfig's authorizeHttpRequests rules. This filter only ever ESTABLISHES
 *     identity; it never itself rejects a request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // strip "Bearer "
        String email;

        try {
            email = jwtService.extractEmail(token);
        } catch (Exception e) {
            // Malformed/expired/tampered token. We don't throw here — we just leave
            // the request unauthenticated and let downstream authorization decide.
            filterChain.doFilter(request, response);
            return;
        }

        boolean alreadyAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null;

        if (email != null && !alreadyAuthenticated) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
