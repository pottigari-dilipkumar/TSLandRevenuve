import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Alert from '../components/Alert';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register, isLoading, error, clearError } = useAuthStore();
  const [form, setForm] = useState({ name: '', email: '', password: '', role: ROLES.CITIZEN });

  const handleSubmit = async (e) => {
    e.preventDefault();
    clearError();
    try {
      await register(form);
      navigate('/dashboard');
    } catch {
      // handled in store
    }
  };

  return (
    <form className="space-y-4" onSubmit={handleSubmit}>
      <Alert message={error} />
      <div>
        <label className="mb-1 block text-sm font-medium">Name</label>
        <input className="input" required value={form.name} onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))} />
      </div>
      <div>
        <label className="mb-1 block text-sm font-medium">Email</label>
        <input className="input" type="email" required value={form.email} onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))} />
      </div>
      <div>
        <label className="mb-1 block text-sm font-medium">Password</label>
        <input className="input" type="password" required minLength={6} value={form.password} onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))} />
      </div>
      <div>
        <label className="mb-1 block text-sm font-medium">Role</label>
        <select className="input" value={form.role} onChange={(e) => setForm((prev) => ({ ...prev, role: e.target.value }))}>
          {Object.values(ROLES).map((role) => (
            <option key={role} value={role}>{role}</option>
          ))}
        </select>
      </div>
      <button className="btn-primary w-full" disabled={isLoading}>{isLoading ? 'Creating account...' : 'Create account'}</button>
      <p className="text-center text-sm text-slate-500">
        Already registered? <Link to="/login" className="text-brand-600">Login</Link>
      </p>
    </form>
  );
}
