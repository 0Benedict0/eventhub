import axiosInstance from './axiosInstance';
import type { Role, User } from '@/types';

export const authApi = {
  register: (data: { email: string; password: string; displayName: string; role: Role }) =>
    axiosInstance.post<{ token: string }>('/api/auth/register', data).then((r) => r.data),

  login: (data: { email: string; password: string }) =>
    axiosInstance.post<{ token: string }>('/api/auth/login', data).then((r) => r.data),

  me: () => axiosInstance.get<User>('/api/auth/me').then((r) => r.data),
};
