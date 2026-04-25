import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';

export default function ProtectedRoute({ roles = [] }) {
  const user = useAuthStore((state) => state.user);

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (roles.length > 0 && !roles.includes(user.role)) {
    // Redirect citizens to their own dashboard
    const fallback = user.role === ROLES.CITIZEN ? '/citizen/dashboard' : '/dashboard';
    return <Navigate to={fallback} replace />;
  }

  return <Outlet />;
}
