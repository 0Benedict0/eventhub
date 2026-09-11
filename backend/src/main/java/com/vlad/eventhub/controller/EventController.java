package com.vlad.eventhub.controller;

import com.vlad.eventhub.dto.request.CreateEventRequest;
import com.vlad.eventhub.dto.response.EventResponse;
import com.vlad.eventhub.entity.User;
import com.vlad.eventhub.repository.UserRepository;
import com.vlad.eventhub.service.EventService;
import com.vlad.eventhub.service.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events")
public class EventController {

    private final EventService eventService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<EventResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.search(keyword, categoryId, from, to, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<EventResponse>> getMine(@AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        return ResponseEntity.ok(eventService.getForOrganizer(user.getId()));
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@AuthenticationPrincipal UserDetails principal,
                                                 @Valid @RequestBody CreateEventRequest request) {
        User user = currentUser(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(user, request));
    }

    @PostMapping("/{id}/cover-image")
    public ResponseEntity<EventResponse> uploadCoverImage(@AuthenticationPrincipal UserDetails principal,
                                                            @PathVariable UUID id,
                                                            @RequestParam("file") MultipartFile file) {
        User user = currentUser(principal);
        String url = fileStorageService.store(file);
        return ResponseEntity.ok(eventService.setCoverImage(id, user, url));
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }
}
