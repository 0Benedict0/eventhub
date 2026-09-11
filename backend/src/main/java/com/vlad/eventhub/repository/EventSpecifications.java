package com.vlad.eventhub.repository;

import com.vlad.eventhub.entity.Event;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Композиційні специфікації для динамічного пошуку/фільтрації подій —
 * замість десятка окремих findByXxxAndYyyAndZzz-методів у репозиторії,
 * що швидко стало б нечитабельним при 4+ незалежних фільтрах одночасно.
 */
public class EventSpecifications {

    public static Specification<Event> keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern));
    }

    public static Specification<Event> hasCategory(UUID categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Event> startsAfter(LocalDateTime from) {
        if (from == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startsAt"), from);
    }

    public static Specification<Event> startsBefore(LocalDateTime to) {
        if (to == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startsAt"), to);
    }

    public static Specification<Event> hasAvailableSeats() {
        return (root, query, cb) -> cb.greaterThan(root.get("availableSeats"), 0);
    }
}
