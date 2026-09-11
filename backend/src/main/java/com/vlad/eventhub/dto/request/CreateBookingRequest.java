package com.vlad.eventhub.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID eventId,
        @Positive int quantity
) {}
