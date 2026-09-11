import { Link } from 'react-router-dom';
import type { EventItem } from '@/types';

function formatDate(iso: string) {
  const d = new Date(iso);
  return d.toLocaleDateString('uk-UA', { day: 'numeric', month: 'short' }) + ' · ' +
    d.toLocaleTimeString('uk-UA', { hour: '2-digit', minute: '2-digit' });
}

export function EventCard({ event }: { event: EventItem }) {
  const soldOut = event.availableSeats === 0;
  const almostGone = !soldOut && event.availableSeats <= event.totalSeats * 0.15;

  return (
    <Link to={`/events/${event.id}`} className="event-card">
      <div className="event-card__image" style={event.coverImageUrl ? { backgroundImage: `url(${event.coverImageUrl})` } : undefined}>
        {!event.coverImageUrl && <span className="event-card__image-fallback">{event.title.slice(0, 1)}</span>}
        <span className="event-card__category">{event.categoryName}</span>
      </div>
      <div className="event-card__body">
        <span className="event-card__date">{formatDate(event.startsAt)}</span>
        <h3 className="event-card__title">{event.title}</h3>
        <span className="event-card__location">{event.location}</span>
        <div className="event-card__footer">
          <span className="event-card__price">{event.price > 0 ? `${event.price} ₴` : 'Безкоштовно'}</span>
          {soldOut ? (
            <span className="badge badge--soldout">Немає місць</span>
          ) : almostGone ? (
            <span className="badge badge--low">Залишилось {event.availableSeats}</span>
          ) : (
            <span className="badge badge--ok">Є місця</span>
          )}
        </div>
      </div>
    </Link>
  );
}
