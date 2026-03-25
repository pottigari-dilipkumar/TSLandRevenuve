import { Link, Outlet } from 'react-router-dom';

export default function AuthLayout() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-brand-50 via-white to-slate-100 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-card">
        <h1 className="text-center text-2xl font-bold text-slate-900">Land Revenue Management</h1>
        <p className="mt-1 text-center text-sm text-slate-500">Secure land and revenue workflows</p>
        <div className="mt-6">
          <Outlet />
        </div>
        <p className="mt-6 text-center text-xs text-slate-500">
          Need help? <Link className="text-brand-600" to="/register">Create account</Link>
        </p>
      </div>
    </div>
  );
}
