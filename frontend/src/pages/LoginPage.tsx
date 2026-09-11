import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await login(email, password);
      navigate('/');
    } catch {
      setError('Невірний email або пароль');
    }
  };

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
      <h1>Вхід у EventHub</h1>
      {error && <p className="form-error">{error}</p>}
      <label>Email<input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>
      <label>Пароль<input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required /></label>
      <button className="btn btn--primary" type="submit">Увійти</button>
      <p>Немає акаунта? <Link to="/register">Зареєструватись</Link></p>
    </form>
  );
}
