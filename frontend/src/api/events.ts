import axiosInstance from './axiosInstance';
import type { Category, EventItem, PageResponse } from '@/types';

export interface SearchParams {
  keyword?: string;
  categoryId?: string;
  page?: number;
}

export const eventsApi = {
  search: (params: SearchParams) =>
    axiosInstance.get<PageResponse<EventItem>>('/api/events', { params: { size: 9, ...params } }).then((r) => r.data),

  getById: (id: string) => axiosInstance.get<EventItem>(`/api/events/${id}`).then((r) => r.data),

  getMine: () => axiosInstance.get<EventItem[]>('/api/events/mine').then((r) => r.data),

  create: (data: {
    title: string; description?: string; categoryId: string; location: string;
    startsAt: string; price: number; totalSeats: number;
  }) => axiosInstance.post<EventItem>('/api/events', data).then((r) => r.data),

  uploadCoverImage: (eventId: string, file: File) => {
    const form = new FormData();
    form.append('file', file);
    return axiosInstance
      .post<EventItem>(`/api/events/${eventId}/cover-image`, form, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then((r) => r.data);
  },

  getCategories: () => axiosInstance.get<Category[]>('/api/categories').then((r) => r.data),
};
