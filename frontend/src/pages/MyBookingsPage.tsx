import { useEffect, useState } from 'react';
import { bookingsApi } from '@/api/bookings';
import type { Booking } from '@/types';

export function MyBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);

  const load = () => bookingsApi.getMine().then(setBookings);
  useEffect(() => { load(); }, []);

  const handleCancel = async (id: string) => {
    await bookingsApi.cancel(id);
    load();
  };

  return (
    <div className="page-container">
      <h1>Мої квитки</h1>
      {bookings.length === 0 && <p className="empty-state">У тебе ще немає заброньованих квитків.</p>}
      <div className="booking-list">
        {bookings.map((b) => (
          <div key={b.id} className={`booking-row ${b.status === 'CANCELLED' ? 'booking-row--cancelled' : ''}`}>
            <div>
              <h3>{b.eventTitle}</h3>
              <p>{new Date(b.eventStartsAt).toLocaleString('uk-UA', { dateStyle: 'medium', timeStyle: 'short' })} · {b.quantity} квиток(ів)</p>
            </div>
            {b.status === 'CONFIRMED' ? (
              <button className="btn btn--ghost" onClick={() => handleCancel(b.id)}>Скасувати</button>
            ) : (
              <span className="badge badge--soldout">Скасовано</span>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
