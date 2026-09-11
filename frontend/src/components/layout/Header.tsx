import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

export function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="header">
      <Link to="/" className="header__logo">
        Event<span>Hub</span>
      </Link>
      <nav className="header__nav">
        {user ? (
          <>
            {user.role === 'ORGANIZER' && <Link to="/organizer">Мої події</Link>}
            {user.role === 'ATTENDEE' && <Link to="/my-bookings">Мої квитки</Link>}
            <span className="header__user">{user.displayName}</span>
            <button className="btn btn--ghost" onClick={() => { logout(); navigate('/'); }}>Вийти</button>
          </>
        ) : (
          <>
            <Link to="/login">Увійти</Link>
            <Link to="/register" className="btn btn--primary">Реєстрація</Link>
          </>
        )}
      </nav>
    </header>
  );
}
