package com.voidkey.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Why JWT instead of server-side sessions:
 * a JWT carries the user's identity in a signed (not encrypted, just tamper-proof)
 * token that the CLIENT stores and re-sends on every request. The server verifies
 * the signature instead of looking up a session in memory/Redis. This is what
 * "stateless" auth means, and it's why our SecurityConfig sets
 * SessionCreationPolicy.STATELESS — we're intentionally not using HttpSession at all.
 *
 * Trade-off worth knowing for interviews: JWTs can't be revoked before they expire
 * without extra infrastructure (a blocklist). That's why expiration-ms is short-ish
 * (24h here) and why a production system would add a refresh-token + revocation list
 * eventually. Flagging that trade-off unprompted in an interview is a strong signal.
 */
@Service
public class JwtService {

    private final Key signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${voidkey.jwt.secret}") String secret,
            @Value("${voidkey.jwt.expiration-ms}") long expirationMs
    ) {
        // HMAC-SHA256 needs a key of at least 256 bits (32 bytes) — this is why the
        // dev-fallback secret in application.yml is deliberately long.
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // email, per our User.getUsername()
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }
}
