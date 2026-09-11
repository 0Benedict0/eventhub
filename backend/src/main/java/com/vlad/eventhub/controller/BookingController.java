package com.vlad.eventhub.controller;

import com.vlad.eventhub.dto.request.CreateBookingRequest;
import com.vlad.eventhub.dto.response.BookingResponse;
import com.vlad.eventhub.entity.User;
import com.vlad.eventhub.repository.UserRepository;
import com.vlad.eventhub.service.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<BookingResponse> book(@AuthenticationPrincipal UserDetails principal,
                                                 @Valid @RequestBody CreateBookingRequest request) {
        User user = currentUser(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.book(user, request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<BookingResponse>> getMine(@AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        return ResponseEntity.ok(bookingService.getForUser(user.getId()));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<BookingResponse>> getForEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(bookingService.getForEvent(eventId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal UserDetails principal, @PathVariable UUID id) {
        User user = currentUser(principal);
        bookingService.cancel(id, user);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }
}
