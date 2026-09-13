package com.voidkey.backend.user;

// Kept deliberately small right now. When you add form-level sharing later
// (owner/editor/viewer on a specific form), that will live on a separate
// FormCollaborator entity — don't be tempted to overload this global Role
// for per-form permissions, they're two different concerns.
public enum Role {
    USER,
    ADMIN
}
