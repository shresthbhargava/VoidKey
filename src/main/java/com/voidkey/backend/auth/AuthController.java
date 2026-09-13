package com.voidkey.backend.auth;

import com.voidkey.backend.auth.dto.AuthResponse;
import com.voidkey.backend.auth.dto.LoginRequest;
import com.voidkey.backend.auth.dto.SignupRequest;
import com.voidkey.backend.user.Role;
import com.voidkey.backend.user.User;
import com.voidkey.backend.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Two endpoints only. Notice signup does NOT go through AuthenticationManager (there's
 * no existing credential to check yet) while login DOES — that asymmetry is the whole
 * point of AuthenticationManager: it's the thing that actually verifies a raw password
 * against the stored hash, via the AuthenticationProvider/PasswordEncoder wired in
 * SecurityConfig.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            // 409 Conflict, not 400 — the request was well-formed, it's the STATE
            // (email already taken) that's the problem. This distinction is a small
            // but real signal of API design maturity.
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getName(), user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // This line does the real work: it loads the user, checks the password against
        // the BCrypt hash, and throws BadCredentialsException if it doesn't match.
        // We don't write any password-comparison code ourselves — never do, for auth.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(); // safe: authenticate() above already proved this user exists

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user.getName(), user.getEmail()));
    }
}
