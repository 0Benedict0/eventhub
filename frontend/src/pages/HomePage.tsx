import { useEffect, useState } from 'react';
import { eventsApi } from '@/api/events';
import { EventCard } from '@/components/events/EventCard';
import type { Category, EventItem, PageResponse } from '@/types';

export function HomePage() {
  const [events, setEvents] = useState<PageResponse<EventItem> | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState<string>('');
  const [page, setPage] = useState(0);

  useEffect(() => { eventsApi.getCategories().then(setCategories); }, []);

  useEffect(() => {
    eventsApi.search({ keyword: keyword || undefined, categoryId: categoryId || undefined, page }).then(setEvents);
  }, [keyword, categoryId, page]);

  const featured = events?.content[0];
  const rest = events?.content.slice(1) ?? [];

  return (
    <div className="home">
      <section className="hero">
        <div className="hero__text">
          <h1>Знайди свою наступну подію</h1>
          <p>Технологічні конференції, концерти, бізнес-івенти та культурні заходи — все в одному місці.</p>
          <input
            className="hero__search"
            placeholder="Пошук за назвою або описом..."
            value={keyword}
            onChange={(e) => { setKeyword(e.target.value); setPage(0); }}
          />
        </div>
      </section>

      <div className="filters">
        <button className={`chip ${categoryId === '' ? 'chip--active' : ''}`} onClick={() => { setCategoryId(''); setPage(0); }}>
          Усі категорії
        </button>
        {categories.map((c) => (
          <button
            key={c.id}
            className={`chip ${categoryId === c.id ? 'chip--active' : ''}`}
            onClick={() => { setCategoryId(c.id); setPage(0); }}
          >
            {c.name}
          </button>
        ))}
      </div>

      {featured && page === 0 && !keyword && !categoryId && (
        <a href={`/events/${featured.id}`} className="featured-event">
          <div className="featured-event__image" style={featured.coverImageUrl ? { backgroundImage: `url(${featured.coverImageUrl})` } : undefined} />
          <div className="featured-event__content">
            <span className="badge badge--ok">Найближча подія</span>
            <h2>{featured.title}</h2>
            <p>{featured.location} · {new Date(featured.startsAt).toLocaleDateString('uk-UA')}</p>
          </div>
        </a>
      )}

      <div className="event-grid">
        {(page === 0 && !keyword && !categoryId ? rest : events?.content ?? []).map((e) => (
          <EventCard key={e.id} event={e} />
        ))}
      </div>

      {events && events.totalPages > 1 && (
        <div className="pagination">
          <button disabled={page === 0} onClick={() => setPage((p) => p - 1)}>← Назад</button>
          <span>{page + 1} / {events.totalPages}</span>
          <button disabled={page >= events.totalPages - 1} onClick={() => setPage((p) => p + 1)}>Далі →</button>
        </div>
      )}
    </div>
  );
}
