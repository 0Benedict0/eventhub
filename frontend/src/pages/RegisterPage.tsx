import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import type { Role } from '@/types';

export function RegisterPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [role, setRole] = useState<Role>('ATTENDEE');
  const [error, setError] = useState<string | null>(null);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await register(email, password, displayName, role);
      navigate('/');
    } catch {
      setError('Не вдалося зареєструватись. Можливо, email вже зайнятий.');
    }
  };

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
      <h1>Реєстрація в EventHub</h1>
      {error && <p className="form-error">{error}</p>}
      <label>Ім'я<input value={displayName} onChange={(e) => setDisplayName(e.target.value)} required /></label>
      <label>Email<input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>
      <label>Пароль<input type="password" minLength={8} value={password} onChange={(e) => setPassword(e.target.value)} required /></label>
      <div className="auth-form__role">
        <label className={role === 'ATTENDEE' ? 'active' : ''}>
          <input type="radio" checked={role === 'ATTENDEE'} onChange={() => setRole('ATTENDEE')} />
          Відвідувач — бронювати квитки
        </label>
        <label className={role === 'ORGANIZER' ? 'active' : ''}>
          <input type="radio" checked={role === 'ORGANIZER'} onChange={() => setRole('ORGANIZER')} />
          Організатор — створювати події
        </label>
      </div>
      <button className="btn btn--primary" type="submit">Зареєструватись</button>
      <p>Вже є акаунт? <Link to="/login">Увійти</Link></p>
    </form>
  );
}
