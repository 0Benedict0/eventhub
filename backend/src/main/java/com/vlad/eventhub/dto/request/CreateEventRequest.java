package com.vlad.eventhub.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateEventRequest(
        @NotBlank String title,
        String description,
        @NotNull UUID categoryId,
        @NotBlank String location,
        @NotNull @Future LocalDateTime startsAt,
        @NotNull @PositiveOrZero BigDecimal price,
        @Positive int totalSeats
) {}
