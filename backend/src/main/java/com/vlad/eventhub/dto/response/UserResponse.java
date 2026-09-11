package com.vlad.eventhub.dto.response;

import com.vlad.eventhub.entity.Role;

import java.util.UUID;

public record UserResponse(UUID id, String email, String displayName, Role role) {}
