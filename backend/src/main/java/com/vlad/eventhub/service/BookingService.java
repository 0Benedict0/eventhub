package com.vlad.eventhub.service;

import com.vlad.eventhub.dto.request.CreateBookingRequest;
import com.vlad.eventhub.dto.response.BookingResponse;
import com.vlad.eventhub.entity.Booking;
import com.vlad.eventhub.entity.Event;
import com.vlad.eventhub.entity.User;
import com.vlad.eventhub.exception.ResourceNotFoundException;
import com.vlad.eventhub.repository.BookingRepository;
import com.vlad.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private static final int MAX_RETRIES = 3;

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final BookingTransactionExecutor transactionExecutor;

    /**
     * Бронювання під конкурентним навантаженням: якщо два користувачі одночасно
     * тиснуть "Забронювати" на останнє місце, обидва читають availableSeats > 0,
     * але @Version на Event гарантує, що лише один UPDATE пройде без конфлікту —
     * другий отримає OptimisticLockingFailureException. Замість одразу віддавати
     * помилку користувачу, робимо кілька спроб перечитати актуальний стан і
     * повторити — кращий UX, ніж миттєва відмова, і дешевше за pessimistic lock
     * (SELECT ... FOR UPDATE), який тримав би рядок заблокованим для всіх
     * читачів на час транзакції.
     */
    @CacheEvict(cacheNames = "events", allEntries = true)
    public BookingResponse book(User user, CreateBookingRequest request) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Booking booking = transactionExecutor.attemptBooking(user, request);
                return toResponse(booking);
            } catch (OptimisticLockingFailureException ex) {
                log.warn("Optimistic lock conflict on event {} booking, attempt {}/{}", request.eventId(), attempt, MAX_RETRIES);
                if (attempt == MAX_RETRIES) throw ex;
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    @Transactional
    @CacheEvict(cacheNames = "events", allEntries = true)
    public void cancel(UUID bookingId, User requester) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getUser().getId().equals(requester.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not your booking");
        }
        if (booking.getStatus() == Booking.Status.CANCELLED) return;

        booking.setStatus(Booking.Status.CANCELLED);
        bookingRepository.save(booking);

        Event event = booking.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + booking.getQuantity());
        eventRepository.save(event);
    }

    public List<BookingResponse> getForUser(UUID userId) {
        return bookingRepository.findAllByUserId(userId).stream().map(this::toResponse).toList();
    }

    public List<BookingResponse> getForEvent(UUID eventId) {
        return bookingRepository.findAllByEventId(eventId).stream().map(this::toResponse).toList();
    }

    private BookingResponse toResponse(Booking b) {
        return new BookingResponse(b.getId(), b.getEvent().getId(), b.getEvent().getTitle(),
                b.getEvent().getStartsAt(), b.getQuantity(), b.getStatus(), b.getCreatedAt());
    }
}
