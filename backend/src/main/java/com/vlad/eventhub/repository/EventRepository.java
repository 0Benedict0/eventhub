package com.vlad.eventhub.repository;

import com.vlad.eventhub.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {
    List<Event> findAllByOrganizerId(UUID organizerId);

    // Використовується scheduled-задачею нагадувань (ReminderScheduler).
    List<Event> findAllByStartsAtBetween(LocalDateTime from, LocalDateTime to);
}
