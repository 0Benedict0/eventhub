package com.vlad.eventhub.dto.response;

public record AuthResponse(String token, String tokenType) {
    public static AuthResponse of(String token) { return new AuthResponse(token, "Bearer"); }
}
