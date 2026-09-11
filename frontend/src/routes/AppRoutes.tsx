import { Routes, Route } from 'react-router-dom';
import { HomePage } from '@/pages/HomePage';
import { EventDetailPage } from '@/pages/EventDetailPage';
import { LoginPage } from '@/pages/LoginPage';
import { RegisterPage } from '@/pages/RegisterPage';
import { MyBookingsPage } from '@/pages/MyBookingsPage';
import { OrganizerDashboard } from '@/pages/OrganizerDashboard';
import { ProtectedRoute } from './ProtectedRoute';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/events/:id" element={<EventDetailPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/my-bookings" element={<ProtectedRoute requireRole="ATTENDEE"><MyBookingsPage /></ProtectedRoute>} />
      <Route path="/organizer" element={<ProtectedRoute requireRole="ORGANIZER"><OrganizerDashboard /></ProtectedRoute>} />
    </Routes>
  );
}
