package com.vlad.eventhub.scheduler;

import com.vlad.eventhub.entity.Booking;
import com.vlad.eventhub.entity.Event;
import com.vlad.eventhub.repository.BookingRepository;
import com.vlad.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Щодня о 9:00 перевіряє, які події відбудуться завтра, і "надсилає"
 * (у pet-проєкті — логує; у продакшені тут був би виклик email/SMS-сервісу
 * або публікація в чергу типу Kafka/SQS) нагадування всім, хто на них
 * забронював квитки. Демонструє фонову асинхронну обробку без залежності
 * від HTTP-запиту користувача — на відміну від Kafka-подій у ShopHub, тут
 * тригер — час, а не подія.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendTomorrowReminders() {
        LocalDateTime from = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime to = from.plusDays(1);

        List<Event> tomorrowEvents = eventRepository.findAllByStartsAtBetween(from, to);
        log.info("Found {} events happening tomorrow", tomorrowEvents.size());

        for (Event event : tomorrowEvents) {
            List<Booking> bookings = bookingRepository.findAllByEventId(event.getId());
            for (Booking booking : bookings) {
                if (booking.getStatus() != Booking.Status.CONFIRMED) continue;
                log.info("[REMINDER EMAIL] To {}: your event '{}' starts tomorrow at {} ({})",
                        booking.getUser().getEmail(), event.getTitle(), event.getStartsAt(), event.getLocation());
            }
        }
    }
}
