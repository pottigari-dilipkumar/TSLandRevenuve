import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import AuthLayout from './layouts/AuthLayout';
import MainLayout from './layouts/MainLayout';
import DashboardPage from './pages/DashboardPage';
import LandFormPage from './pages/LandFormPage';
import LandRecordsPage from './pages/LandRecordsPage';
import LoginPage from './pages/LoginPage';
import NotFoundPage from './pages/NotFoundPage';
import OwnerFormPage from './pages/OwnerFormPage';
import RegisterPage from './pages/RegisterPage';
import RevenueDetailsPage from './pages/RevenueDetailsPage';
import UserManagementPage from './pages/UserManagementPage';
import CitizenDashboardPage from './pages/CitizenDashboardPage';
import CitizenProfilePage from './pages/CitizenProfilePage';
import MarketValuesPage from './pages/MarketValuesPage';
import RegistrationFormPage from './pages/RegistrationFormPage';
import RegistrationListPage from './pages/RegistrationListPage';
import RegistrationDetailPage from './pages/RegistrationDetailPage';
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
          {/* Staff dashboard */}
          <Route element={<ProtectedRoute roles={[ROLES.ADMIN, ROLES.REVENUE_OFFICER, ROLES.DATA_ENTRY]} />}>
            <Route path="/dashboard" element={<DashboardPage />} />
          </Route>

          {/* Citizen routes */}
          <Route element={<ProtectedRoute roles={[ROLES.CITIZEN]} />}>
            <Route path="/citizen/dashboard" element={<CitizenDashboardPage />} />
            <Route path="/citizen/profile" element={<CitizenProfilePage />} />
          </Route>

          {/* Market values — all roles */}
          <Route path="/market-values" element={<MarketValuesPage />} />

          {/* Land records */}
          <Route path="/lands" element={<LandRecordsPage />} />
          <Route element={<ProtectedRoute roles={[ROLES.ADMIN, ROLES.DATA_ENTRY]} />}>
            <Route path="/lands/new" element={<LandFormPage />} />
            <Route path="/lands/:id/edit" element={<LandFormPage />} />
          </Route>

          {/* Revenue */}
          <Route element={<ProtectedRoute roles={[ROLES.ADMIN, ROLES.REVENUE_OFFICER]} />}>
            <Route path="/revenue" element={<RevenueDetailsPage />} />
          </Route>

          {/* Registrations */}
          <Route element={<ProtectedRoute roles={[ROLES.SRO, ROLES.SRO_ASSISTANT, ROLES.ADMIN, ROLES.REVENUE_OFFICER]} />}>
            <Route path="/registrations" element={<RegistrationListPage />} />
            <Route path="/registrations/:ref" element={<RegistrationDetailPage />} />
          </Route>
          <Route element={<ProtectedRoute roles={[ROLES.SRO, ROLES.SRO_ASSISTANT, ROLES.ADMIN]} />}>
            <Route path="/registrations/new" element={<RegistrationFormPage />} />
          </Route>

          {/* Admin */}
          <Route element={<ProtectedRoute roles={[ROLES.ADMIN]} />}>
            <Route path="/owners/new" element={<OwnerFormPage />} />
            <Route path="/users" element={<UserManagementPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
