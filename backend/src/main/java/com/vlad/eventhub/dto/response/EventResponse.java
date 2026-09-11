package com.vlad.eventhub.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Serializable — цей DTO кешується в Redis (GenericJackson2JsonRedisSerializer серіалізує в JSON, але запис через Serializable — гарна практика на випадок зміни серіалізатора).
public record EventResponse(
        UUID id, String title, String description, String categoryName, String organizerName,
        String location, LocalDateTime startsAt, BigDecimal price,
        int totalSeats, int availableSeats, String coverImageUrl
) implements Serializable {}
