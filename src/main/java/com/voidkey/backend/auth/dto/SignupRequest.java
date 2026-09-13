package com.voidkey.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Why validate here AND not just rely on the database's NOT NULL/UNIQUE constraints:
// DB constraints are your last line of defense (they catch bugs and race conditions),
// but returning a clean 400 with a clear message at the API boundary is what makes
// your API pleasant to build a frontend against. Validate at the edge, enforce again
// at the data layer — both matter, for different reasons.
public record SignupRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {}
