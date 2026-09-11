CREATE INDEX idx_events_category ON events (category_id);
CREATE INDEX idx_events_organizer ON events (organizer_id);
CREATE INDEX idx_events_starts_at ON events (starts_at);
CREATE INDEX idx_bookings_user ON bookings (user_id);
CREATE INDEX idx_bookings_event ON bookings (event_id);
