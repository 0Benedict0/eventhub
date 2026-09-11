package com.vlad.eventhub.service;

import com.vlad.eventhub.dto.request.CreateBookingRequest;
import com.vlad.eventhub.entity.Booking;
import com.vlad.eventhub.entity.Event;
import com.vlad.eventhub.entity.User;
import com.vlad.eventhub.exception.ResourceNotFoundException;
import com.vlad.eventhub.exception.SoldOutException;
import com.vlad.eventhub.repository.BookingRepository;
import com.vlad.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Винесено в окремий бін навмисно: якби метод attemptBooking() був
 * приватним/protected методом у BookingService і викликався звідти ж
 * (this.attemptBooking()), Spring AOP-проксі для @Transactional не
 * спрацював би — самовиклик обходить проксі, і OptimisticLockingFailureException
 * не спричинив би реальний rollback транзакції. Через окремий Spring-бін
 * виклик завжди йде через проксі, і @Transactional працює коректно.
 */
@Component
@RequiredArgsConstructor
class BookingTransactionExecutor {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    @Transactional
    public Booking attemptBooking(User user, CreateBookingRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + request.eventId()));

        if (event.getAvailableSeats() < request.quantity()) {
            throw new SoldOutException("Only " + event.getAvailableSeats() + " seats left for this event");
        }

        event.setAvailableSeats(event.getAvailableSeats() - request.quantity());
        eventRepository.save(event); // тут перевіряється @Version — можливий OptimisticLockingFailureException

        Booking booking = Booking.builder()
                .event(event).user(user).quantity(request.quantity()).status(Booking.Status.CONFIRMED)
                .build();
        return bookingRepository.save(booking);
    }
}
