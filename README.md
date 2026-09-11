# EventHub — Event Management & Ticket Booking Platform

## Overview

EventHub — платформа для подій і бронювання квитків: організатори
створюють події з обмеженою кількістю місць, відвідувачі бронюють квитки.
Головний технічний фокус — коректна робота під конкурентним навантаженням
(бронювання останніх місць) і практики, яких не було в попередніх
Java-проєктах (FinTrack, TaskFlow, ShopHub): optimistic locking, Redis-кеш
GET-запитів, завантаження файлів, scheduled-задачі, динамічний пошук через
Specifications.

## Architecture

Детально — [`docs/architecture.md`](docs/architecture.md) (конкурентне
бронювання, self-invocation problem) та [`docs/rbac.md`](docs/rbac.md).

- Java 21, Spring Boot 3.3, шарувата архітектура Controller → Service → Repository
- PostgreSQL + Flyway, Redis (кеш + майбутнє розширення для сесій)
- JWT-автентифікація, `@PreAuthorize` для рольового доступу (ATTENDEE / ORGANIZER)
- React 18 + TypeScript + Vite, без зайвих UI-бібліотек — ручна дизайн-система

## Features

- Пошук/фільтрація подій за ключовим словом, категорією, датою — пагінація
- Бронювання квитків, захищене від перепродажу місць (optimistic locking + retry)
- Скасування бронювання з поверненням місць у наявність
- Кабінет організатора: створення подій, завантаження обкладинки (`multipart/form-data`)
- Кешування списку подій у Redis з інвалідацією при зміні даних
- Щоденні нагадування про завтрашні події (`@Scheduled`, лог замість реального email)

## Tech Stack

| Категорія  | Технологія                                                 |
| ---------- | ---------------------------------------------------------- |
| Backend    | Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA |
| Frontend   | React 18, TypeScript, Vite, React Router, Axios            |
| DB / Cache | PostgreSQL 16, Flyway, Redis 7 (Spring Cache abstraction)  |
| Auth       | JWT (jjwt), BCrypt                                         |
| Testing    | JUnit 5, Mockito (retry-логіка бронювання окремим тестом)  |
| Infra      | Docker, Docker Compose, GitHub Actions                     |

## Database Schema

```
users (id, email, password_hash, display_name, role)
   │
   ├── events (id, title, category_id, organizer_id, starts_at,
   │           total_seats, available_seats, version, ...)
   │      │
   │      └── bookings (id, event_id, user_id, quantity, status)
   │
categories (id, name)
```

`events.version` — колонка для optimistic locking (`@Version` у JPA).

## API Documentation

Swagger: **http://localhost:8080/swagger-ui.html**

| Метод  | Шлях                                | Auth      | Опис                                     |
| ------ | ----------------------------------- | --------- | ---------------------------------------- |
| GET    | /api/events?keyword&categoryId&page | ні        | Пошук/список подій (кешовано)            |
| GET    | /api/events/{id}                    | ні        | Деталі події                             |
| POST   | /api/events                         | ORGANIZER | Створити подію                           |
| POST   | /api/events/{id}/cover-image        | ORGANIZER | Завантажити обкладинку (multipart)       |
| POST   | /api/bookings                       | ATTENDEE  | Забронювати квиток (з retry на конфлікт) |
| DELETE | /api/bookings/{id}                  | власник   | Скасувати бронювання                     |

## How to Run

```bash
docker-compose up --build
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

## Tests

```bash
cd backend && ./mvnw test
```

Окремий тест (`BookingServiceConcurrencyTest`) перевіряє саме retry-логіку:
що сервіс справді повторює спробу при `OptimisticLockingFailureException`,
а не просто падає з першого разу
