import { useEffect, useState } from 'react';
import { Plus, X } from 'lucide-react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

const STAFF_ROLES = ['ADMIN', 'REVENUE_OFFICER', 'DATA_ENTRY', 'SRO', 'SRO_ASSISTANT'];

const ROLE_BADGE = {
  ADMIN: 'bg-purple-100 text-purple-800',
  REVENUE_OFFICER: 'bg-blue-100 text-blue-800',
  DATA_ENTRY: 'bg-slate-100 text-slate-700',
  SRO: 'bg-green-100 text-green-800',
  SRO_ASSISTANT: 'bg-teal-100 text-teal-800',
  CITIZEN: 'bg-orange-100 text-orange-800',
};

const emptyForm = { username: '', password: '', role: 'SRO_ASSISTANT', fullName: '', mobile: '', email: '' };

export default function UserManagementPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');
  const [success, setSuccess] = useState('');

  const loadUsers = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await landApi.getUsers();
      setUsers(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadUsers(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setFormError('');
    setSaving(true);
    try {
      await landApi.createUser(form);
      setSuccess(`User "${form.username}" created successfully.`);
      setForm(emptyForm);
      setShowForm(false);
      loadUsers();
    } catch (err) {
      setFormError(err?.response?.data?.message || 'Failed to create user');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">User Management</h2>
        <button className="btn-primary flex items-center gap-2" onClick={() => { setShowForm(true); setFormError(''); setSuccess(''); }}>
          <Plus size={16} /> Create Staff User
        </button>
      </div>

      <Alert message={success} type="success" />
      <Alert message={error} />

      {/* Create user form */}
      {showForm && (
        <div className="card space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-semibold">New Staff User</h3>
            <button onClick={() => setShowForm(false)} className="text-slate-400 hover:text-slate-600"><X size={18} /></button>
          </div>
          <Alert message={formError} />
          <form className="grid grid-cols-1 gap-4 sm:grid-cols-2" onSubmit={handleCreate}>
            <div>
              <label className="mb-1 block text-sm font-medium">Username *</label>
              <input className="input" required value={form.username}
                onChange={(e) => setForm((p) => ({ ...p, username: e.target.value }))} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Password *</label>
              <input className="input" type="password" required minLength={6} value={form.password}
                onChange={(e) => setForm((p) => ({ ...p, password: e.target.value }))} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Role *</label>
              <select className="input" required value={form.role}
                onChange={(e) => setForm((p) => ({ ...p, role: e.target.value }))}>
                {STAFF_ROLES.map((r) => (
                  <option key={r} value={r}>{r.replace('_', ' ')}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Full Name</label>
              <input className="input" value={form.fullName}
                onChange={(e) => setForm((p) => ({ ...p, fullName: e.target.value }))} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Mobile</label>
              <input className="input" type="tel" maxLength={10} value={form.mobile}
                onChange={(e) => setForm((p) => ({ ...p, mobile: e.target.value.replace(/\D/g, '') }))} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Email</label>
              <input className="input" type="email" value={form.email}
                onChange={(e) => setForm((p) => ({ ...p, email: e.target.value }))} />
            </div>
            <div className="sm:col-span-2 flex justify-end gap-3">
              <button type="button" className="btn-secondary" onClick={() => setShowForm(false)}>Cancel</button>
              <button className="btn-primary" disabled={saving}>
                {saving ? 'Creating...' : 'Create User'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* User list */}
      {loading ? (
        <p className="text-slate-500">Loading users...</p>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3">ID</th>
                <th className="px-4 py-3">Username</th>
                <th className="px-4 py-3">Full Name</th>
                <th className="px-4 py-3">Role</th>
                <th className="px-4 py-3">Mobile</th>
                <th className="px-4 py-3">Email</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {users.map((u) => (
                <tr key={u.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 text-slate-400">{u.id}</td>
                  <td className="px-4 py-3 font-medium">{u.username}</td>
                  <td className="px-4 py-3">{u.fullName || <span className="text-slate-300">—</span>}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${ROLE_BADGE[u.role] || 'bg-slate-100 text-slate-700'}`}>
                      {u.role.replace('_', ' ')}
                    </span>
                  </td>
                  <td className="px-4 py-3">{u.mobile || <span className="text-slate-300">—</span>}</td>
                  <td className="px-4 py-3">{u.email || <span className="text-slate-300">—</span>}</td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr><td colSpan={6} className="px-4 py-8 text-center text-slate-400">No users found</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
