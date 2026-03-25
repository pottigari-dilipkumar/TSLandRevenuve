import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import AuthLayout from './layouts/AuthLayout';
import MainLayout from './layouts/MainLayout';
import DashboardPage from './pages/DashboardPage';
import LandFormPage from './pages/LandFormPage';
import LandRecordsPage from './pages/LandRecordsPage';
import LoginPage from './pages/LoginPage';
import NotFoundPage from './pages/NotFoundPage';
import RegisterPage from './pages/RegisterPage';
import RevenueDetailsPage from './pages/RevenueDetailsPage';
import UserManagementPage from './pages/UserManagementPage';
import { ROLES } from './utils/roles';

export default function App() {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/lands" element={<LandRecordsPage />} />

          <Route element={<ProtectedRoute roles={[ROLES.ADMIN, ROLES.REVENUE_OFFICER, ROLES.DATA_ENTRY]} />}>
            <Route path="/lands/new" element={<LandFormPage />} />
          </Route>

          <Route element={<ProtectedRoute roles={[ROLES.ADMIN, ROLES.REVENUE_OFFICER]} />}>
            <Route path="/revenue" element={<RevenueDetailsPage />} />
          </Route>

          <Route element={<ProtectedRoute roles={[ROLES.ADMIN]} />}>
            <Route path="/users" element={<UserManagementPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
