import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { eventsApi } from '@/api/events';
import { bookingsApi } from '@/api/bookings';
import { useAuth } from '@/context/AuthContext';
import type { EventItem } from '@/types';

export function EventDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const [event, setEvent] = useState<EventItem | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [status, setStatus] = useState<'idle' | 'booking' | 'done' | 'error'>('idle');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => { if (id) eventsApi.getById(id).then(setEvent); }, [id]);

  if (!event) return <div className="loading-screen">Завантаження...</div>;

  const soldOut = event.availableSeats === 0;

  const handleBook = async () => {
    if (!id) return;
    setStatus('booking');
    setErrorMsg('');
    try {
      await bookingsApi.book(id, quantity);
      setStatus('done');
      // Оновлюємо лічильник місць одразу з відповіді сервера (не з кешу) —
      // саме тому, що бекенд захищений optimistic locking, тут завжди
      // актуальне число, а не те, що могло встигнути змінитись у сусідній вкладці.
      const fresh = await eventsApi.getById(id);
      setEvent(fresh);
    } catch (err: any) {
      setStatus('error');
      setErrorMsg(err?.response?.data?.message ?? 'Не вдалося забронювати. Спробуйте ще раз.');
    }
  };

  return (
    <div className="event-detail">
      <div className="event-detail__cover" style={event.coverImageUrl ? { backgroundImage: `url(${event.coverImageUrl})` } : undefined} />

      <div className="event-detail__content">
        <div className="event-detail__main">
          <span className="badge badge--ok">{event.categoryName}</span>
          <h1>{event.title}</h1>
          <p className="event-detail__meta">
            {new Date(event.startsAt).toLocaleString('uk-UA', { dateStyle: 'full', timeStyle: 'short' })} · {event.location}
          </p>
          <p className="event-detail__organizer">Організатор: {event.organizerName}</p>
          <p className="event-detail__description">{event.description}</p>
        </div>

        <aside className="booking-card">
          <div className="booking-card__price">{event.price > 0 ? `${event.price} ₴` : 'Безкоштовно'}</div>
          <div className="booking-card__seats">
            {soldOut ? 'Місць немає' : `Вільно: ${event.availableSeats} з ${event.totalSeats}`}
          </div>

          {!soldOut && user?.role === 'ATTENDEE' && (
            <>
              <label className="booking-card__qty">
                Кількість квитків
                <input
                  type="number" min={1} max={event.availableSeats}
                  value={quantity}
                  onChange={(e) => setQuantity(Math.max(1, Math.min(event.availableSeats, Number(e.target.value))))}
                />
              </label>
              <button className="btn btn--primary btn--block" disabled={status === 'booking'} onClick={handleBook}>
                {status === 'booking' ? 'Бронюємо...' : 'Забронювати'}
              </button>
              {status === 'done' && <p className="booking-card__success">Заброньовано! Квиток у розділі «Мої квитки».</p>}
              {status === 'error' && <p className="booking-card__error">{errorMsg}</p>}
            </>
          )}

          {!user && <p className="booking-card__hint">Увійдіть, щоб забронювати квиток.</p>}
          {soldOut && <button className="btn btn--disabled" disabled>Немає вільних місць</button>}
        </aside>
      </div>
    </div>
  );
}
