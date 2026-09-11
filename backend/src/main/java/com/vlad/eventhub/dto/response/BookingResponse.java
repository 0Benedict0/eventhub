package com.vlad.eventhub.dto.response;

import com.vlad.eventhub.entity.Booking;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID id, UUID eventId, String eventTitle, LocalDateTime eventStartsAt,
        int quantity, Booking.Status status, Instant createdAt
) {}
