import { useEffect, useRef, useState, type FormEvent } from 'react';
import { eventsApi } from '@/api/events';
import type { Category, EventItem } from '@/types';

export function OrganizerDashboard() {
  const [myEvents, setMyEvents] = useState<EventItem[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [showForm, setShowForm] = useState(false);

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [location, setLocation] = useState('');
  const [startsAt, setStartsAt] = useState('');
  const [price, setPrice] = useState(0);
  const [totalSeats, setTotalSeats] = useState(50);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const load = () => eventsApi.getMine().then(setMyEvents);

  useEffect(() => {
    load();
    eventsApi.getCategories().then((cats) => {
      setCategories(cats);
      if (cats.length) setCategoryId(cats[0].id);
    });
  }, []);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    const created = await eventsApi.create({
      title, description, categoryId, location,
      startsAt: new Date(startsAt).toISOString(),
      price, totalSeats,
    });

    const file = fileInputRef.current?.files?.[0];
    if (file) await eventsApi.uploadCoverImage(created.id, file);

    setShowForm(false);
    setTitle(''); setDescription(''); setLocation(''); setPrice(0); setTotalSeats(50);
    load();
  };

  return (
    <div className="page-container">
      <div className="page-container__header">
        <h1>Мої події</h1>
        <button className="btn btn--primary" onClick={() => setShowForm((s) => !s)}>
          {showForm ? 'Закрити' : '+ Нова подія'}
        </button>
      </div>

      {showForm && (
        <form className="event-form" onSubmit={handleCreate}>
          <label>Назва<input value={title} onChange={(e) => setTitle(e.target.value)} required /></label>
          <label>Опис<textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} /></label>
          <div className="event-form__row">
            <label>Категорія
              <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </label>
            <label>Локація<input value={location} onChange={(e) => setLocation(e.target.value)} required /></label>
          </div>
          <div className="event-form__row">
            <label>Дата і час<input type="datetime-local" value={startsAt} onChange={(e) => setStartsAt(e.target.value)} required /></label>
            <label>Ціна (₴)<input type="number" min={0} value={price} onChange={(e) => setPrice(Number(e.target.value))} /></label>
            <label>Місць<input type="number" min={1} value={totalSeats} onChange={(e) => setTotalSeats(Number(e.target.value))} /></label>
          </div>
          <label>Обкладинка<input type="file" accept="image/*" ref={fileInputRef} /></label>
          <button className="btn btn--primary" type="submit">Створити подію</button>
        </form>
      )}

      <div className="organizer-table">
        {myEvents.map((e) => (
          <div key={e.id} className="organizer-row">
            <span>{e.title}</span>
            <span>{new Date(e.startsAt).toLocaleDateString('uk-UA')}</span>
            <span>{e.availableSeats} / {e.totalSeats} вільно</span>
          </div>
        ))}
        {myEvents.length === 0 && <p className="empty-state">Ще немає створених подій.</p>}
      </div>
    </div>
  );
}
