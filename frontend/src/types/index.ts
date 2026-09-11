export type Role = 'ATTENDEE' | 'ORGANIZER';
export type BookingStatus = 'CONFIRMED' | 'CANCELLED';

export interface User {
  id: string;
  email: string;
  displayName: string;
  role: Role;
}

export interface Category {
  id: string;
  name: string;
}

export interface EventItem {
  id: string;
  title: string;
  description: string | null;
  categoryName: string;
  organizerName: string;
  location: string;
  startsAt: string;
  price: number;
  totalSeats: number;
  availableSeats: number;
  coverImageUrl: string | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface Booking {
  id: string;
  eventId: string;
  eventTitle: string;
  eventStartsAt: string;
  quantity: number;
  status: BookingStatus;
  createdAt: string;
}
