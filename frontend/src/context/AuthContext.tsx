import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { authApi } from '@/api/auth';
import type { Role, User } from '@/types';

interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, displayName: string, role: Role) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('eventhub_token');
    if (!token) { setIsLoading(false); return; }
    authApi.me().then(setUser).catch(() => localStorage.removeItem('eventhub_token')).finally(() => setIsLoading(false));
  }, []);

  const login = async (email: string, password: string) => {
    const { token } = await authApi.login({ email, password });
    localStorage.setItem('eventhub_token', token);
    setUser(await authApi.me());
  };

  const register = async (email: string, password: string, displayName: string, role: Role) => {
    const { token } = await authApi.register({ email, password, displayName, role });
    localStorage.setItem('eventhub_token', token);
    setUser(await authApi.me());
  };

  const logout = () => {
    localStorage.removeItem('eventhub_token');
    setUser(null);
  };

  return <AuthContext.Provider value={{ user, isLoading, login, register, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
