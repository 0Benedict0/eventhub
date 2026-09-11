package com.vlad.eventhub.repository;

import com.vlad.eventhub.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findAllByUserId(UUID userId);
    List<Booking> findAllByEventId(UUID eventId);
}
