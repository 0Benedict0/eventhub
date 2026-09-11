package com.vlad.eventhub.service;

import com.vlad.eventhub.dto.request.CreateEventRequest;
import com.vlad.eventhub.dto.response.EventResponse;
import com.vlad.eventhub.entity.Category;
import com.vlad.eventhub.entity.Event;
import com.vlad.eventhub.entity.User;
import com.vlad.eventhub.exception.ResourceNotFoundException;
import com.vlad.eventhub.repository.CategoryRepository;
import com.vlad.eventhub.repository.EventRepository;
import com.vlad.eventhub.repository.EventSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    @PreAuthorize("hasRole('ORGANIZER')")
    @Transactional
    @CacheEvict(cacheNames = "events", allEntries = true)
    public EventResponse create(User organizer, CreateEventRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));

        Event event = Event.builder()
                .title(request.title())
                .description(request.description())
                .category(category)
                .organizer(organizer)
                .location(request.location())
                .startsAt(request.startsAt())
                .price(request.price())
                .totalSeats(request.totalSeats())
                .availableSeats(request.totalSeats())
                .build();

        eventRepository.save(event);
        return toResponse(event);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @Transactional
    @CacheEvict(cacheNames = "events", allEntries = true)
    public EventResponse setCoverImage(UUID eventId, User requester, String imageUrl) {
        Event event = getOwnedEvent(eventId, requester);
        event.setCoverImageUrl(imageUrl);
        eventRepository.save(event);
        return toResponse(event);
    }

    /**
     * Кешується в Redis на 60с (див. CacheConfig) — це найчастіший запит на
     * публічній сторінці. Ключ будується з усіх параметрів пошуку, щоб різні
     * комбінації фільтрів не перезаписували кеш одна одної.
     */
    @Cacheable(cacheNames = "events", key = "#keyword + '_' + #categoryId + '_' + #from + '_' + #to + '_' + #pageable.pageNumber")
    public Page<EventResponse> search(String keyword, UUID categoryId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<Event> spec = Specification.where(EventSpecifications.keywordContains(keyword))
                .and(EventSpecifications.hasCategory(categoryId))
                .and(EventSpecifications.startsAfter(from))
                .and(EventSpecifications.startsBefore(to));

        return eventRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public EventResponse getById(UUID id) {
        return toResponse(getEventOrThrow(id));
    }

    public List<EventResponse> getForOrganizer(UUID organizerId) {
        return eventRepository.findAllByOrganizerId(organizerId).stream().map(this::toResponse).toList();
    }

    Event getEventOrThrow(UUID id) {
        return eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }

    private Event getOwnedEvent(UUID eventId, User requester) {
        Event event = getEventOrThrow(eventId);
        if (!event.getOrganizer().getId().equals(requester.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not the owner of this event");
        }
        return event;
    }

    EventResponse toResponse(Event e) {
        return new EventResponse(
                e.getId(), e.getTitle(), e.getDescription(), e.getCategory().getName(), e.getOrganizer().getDisplayName(),
                e.getLocation(), e.getStartsAt(), e.getPrice(), e.getTotalSeats(), e.getAvailableSeats(), e.getCoverImageUrl());
    }
}
