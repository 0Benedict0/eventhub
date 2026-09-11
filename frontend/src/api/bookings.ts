import axiosInstance from './axiosInstance';
import type { Booking } from '@/types';

export const bookingsApi = {
  book: (eventId: string, quantity: number) =>
    axiosInstance.post<Booking>('/api/bookings', { eventId, quantity }).then((r) => r.data),

  getMine: () => axiosInstance.get<Booking[]>('/api/bookings/mine').then((r) => r.data),

  cancel: (id: string) => axiosInstance.delete(`/api/bookings/${id}`),
};
