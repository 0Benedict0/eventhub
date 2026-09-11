package com.vlad.eventhub.service;

import com.vlad.eventhub.dto.request.CreateBookingRequest;
import com.vlad.eventhub.entity.Booking;
import com.vlad.eventhub.entity.User;
import com.vlad.eventhub.repository.BookingRepository;
import com.vlad.eventhub.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Перевіряє, що BookingService дійсно повторює спробу при конфлікті
 * версії (а не просто падає з першої), і здається після вичерпання ліміту.
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceConcurrencyTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private EventRepository eventRepository;
    @Mock private BookingTransactionExecutor transactionExecutor;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void retriesOnOptimisticLockConflict_thenGivesUpAfterMaxAttempts() {
        User user = User.builder().id(UUID.randomUUID()).build();
        CreateBookingRequest request = new CreateBookingRequest(UUID.randomUUID(), 1);

        when(transactionExecutor.attemptBooking(any(), any()))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        assertThrows(OptimisticLockingFailureException.class, () -> bookingService.book(user, request));

        // 3 спроби (MAX_RETRIES), а не одна — саме це і є сенс retry-логіки.
        verify(transactionExecutor, times(3)).attemptBooking(any(), any());
    }

    @Test
    void succeedsOnSecondAttempt_afterOneConflict() {
        User user = User.builder().id(UUID.randomUUID()).build();
        CreateBookingRequest request = new CreateBookingRequest(UUID.randomUUID(), 1);
        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .event(com.vlad.eventhub.entity.Event.builder().id(request.eventId()).title("Demo").build())
                .user(user).quantity(1).status(Booking.Status.CONFIRMED).build();

        when(transactionExecutor.attemptBooking(any(), any()))
                .thenThrow(new OptimisticLockingFailureException("conflict"))
                .thenReturn(booking);

        var response = bookingService.book(user, request);

        assertEqualsSafe(booking.getId(), response.id());
        verify(transactionExecutor, times(2)).attemptBooking(any(), any());
    }

    private void assertEqualsSafe(Object expected, Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
