import { useEffect, useState } from 'react';
import { Plus, X, ChevronLeft, ChevronRight, Search } from 'lucide-react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

const STAFF_ROLES = ['ADMIN', 'REVENUE_OFFICER', 'DATA_ENTRY', 'SRO', 'SRO_ASSISTANT'];
const STAFF_ROLE_SET = new Set(['ADMIN', 'REVENUE_OFFICER', 'DATA_ENTRY', 'SRO', 'SRO_ASSISTANT']);
const CITIZEN_ROLE_SET = new Set(['CITIZEN']);

const ROLE_BADGE = {
  ADMIN: 'bg-purple-100 text-purple-800',
  REVENUE_OFFICER: 'bg-blue-100 text-blue-800',
  DATA_ENTRY: 'bg-slate-100 text-slate-700',
  SRO: 'bg-green-100 text-green-800',
  SRO_ASSISTANT: 'bg-teal-100 text-teal-800',
  CITIZEN: 'bg-orange-100 text-orange-800',
};

const emptyForm = { username: '', password: '', role: 'SRO_ASSISTANT', fullName: '', mobile: '', email: '' };
const PAGE_SIZE = 10;

function Pagination({ page, totalPages, onPrev, onNext }) {
  if (totalPages <= 1) return null;
  return (
    <div className="flex items-center justify-end gap-2 px-4 py-3 border-t border-slate-200">
      <button
        className="inline-flex items-center gap-1 rounded px-2 py-1 text-sm text-slate-500 hover:bg-slate-100 disabled:opacity-40"
        onClick={onPrev}
        disabled={page === 0}
      >
        <ChevronLeft size={15} /> Prev
      </button>
      <span className="text-sm text-slate-500">
        Page {page + 1} of {totalPages}
      </span>
      <button
        className="inline-flex items-center gap-1 rounded px-2 py-1 text-sm text-slate-500 hover:bg-slate-100 disabled:opacity-40"
        onClick={onNext}
        disabled={page >= totalPages - 1}
      >
        Next <ChevronRight size={15} />
      </button>
    </div>
  );
}

export default function UserManagementPage() {
  const [activeTab, setActiveTab] = useState('staff');
  const [allUsers, setAllUsers] = useState([]);   // full list from server (used for client-side search)
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [mobileSearch, setMobileSearch] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');
  const [success, setSuccess] = useState('');

  const loadUsers = async (tab = activeTab, pg = page) => {
    setLoading(true);
    setError('');
    try {
      const data = await landApi.getUsers({ type: tab, page: pg, size: PAGE_SIZE });
      if (Array.isArray(data)) {
        // old server: flat array — filter by role client-side
        const roleSet = tab === 'citizen' ? CITIZEN_ROLE_SET : STAFF_ROLE_SET;
        const filtered = data.filter((u) => roleSet.has(u.role));
        setAllUsers(filtered);
        setTotalPages(1);
        setTotalElements(filtered.length);
      } else {
        setAllUsers(data.content || []);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      }
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadUsers(activeTab, page); }, [activeTab, page]);

  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setPage(0);
    setMobileSearch('');
    setSuccess('');
    setError('');
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setFormError('');
    setSaving(true);
    try {
      await landApi.createUser(form);
      setSuccess(`User "${form.username}" created successfully.`);
      setForm(emptyForm);
      setShowForm(false);
      if (activeTab !== 'staff') {
        setActiveTab('staff');
        setPage(0);
      } else {
        loadUsers('staff', 0);
      }
    } catch (err) {
      setFormError(err?.response?.data?.message || 'Failed to create user');
    } finally {
      setSaving(false);
    }
  };

  // Client-side mobile filter applied on top of the loaded page
  const displayedUsers = mobileSearch
    ? allUsers.filter((u) => (u.mobile || '').includes(mobileSearch.trim()))
    : allUsers;

  const tabs = [
    { key: 'staff', label: 'Staff Users' },
    { key: 'citizen', label: 'Citizens' },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">User Management</h2>
        <button
          className="btn-primary flex items-center gap-2"
          onClick={() => { setShowForm(true); setFormError(''); setSuccess(''); }}
        >
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
            <button onClick={() => setShowForm(false)} className="text-slate-400 hover:text-slate-600">
              <X size={18} />
            </button>
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

      {/* Tabs */}
      <div className="border-b border-slate-200">
        <nav className="flex gap-6">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              onClick={() => handleTabChange(tab.key)}
              className={`pb-2 text-sm font-medium border-b-2 transition-colors ${
                activeTab === tab.key
                  ? 'border-brand-600 text-brand-700'
                  : 'border-transparent text-slate-500 hover:text-slate-700'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </nav>
      </div>

      {/* Mobile search */}
      <div className="relative max-w-xs">
        <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          className="input pl-8 text-sm"
          placeholder="Search by mobile number"
          value={mobileSearch}
          maxLength={10}
          onChange={(e) => setMobileSearch(e.target.value.replace(/\D/g, ''))}
        />
      </div>

      {/* User list */}
      {loading ? (
        <p className="text-slate-500">Loading users...</p>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
          <div className="px-4 py-2 text-xs text-slate-400 border-b border-slate-100">
            {mobileSearch
              ? `${displayedUsers.length} result${displayedUsers.length !== 1 ? 's' : ''} for mobile "${mobileSearch}"`
              : `${totalElements} ${activeTab === 'citizen' ? 'citizen' : 'staff'} user${totalElements !== 1 ? 's' : ''}`}
          </div>
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3">#</th>
                <th className="px-4 py-3">Username</th>
                <th className="px-4 py-3">Full Name</th>
                <th className="px-4 py-3">Role</th>
                <th className="px-4 py-3">Mobile</th>
                <th className="px-4 py-3">Email</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {displayedUsers.map((u, idx) => (
                <tr key={u.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 text-slate-400">{page * PAGE_SIZE + idx + 1}</td>
                  <td className="px-4 py-3 font-medium">{u.username}</td>
                  <td className="px-4 py-3">{u.fullName || <span className="text-slate-300">—</span>}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${ROLE_BADGE[u.role] || 'bg-slate-100 text-slate-700'}`}>
                      {u.role.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="px-4 py-3">{u.mobile || <span className="text-slate-300">—</span>}</td>
                  <td className="px-4 py-3">{u.email || <span className="text-slate-300">—</span>}</td>
                </tr>
              ))}
              {displayedUsers.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-slate-400">
                    {mobileSearch ? `No users found with mobile "${mobileSearch}"` : `No ${activeTab === 'citizen' ? 'citizen' : 'staff'} users found`}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          {!mobileSearch && (
            <Pagination
              page={page}
              totalPages={totalPages}
              onPrev={() => setPage((p) => p - 1)}
              onNext={() => setPage((p) => p + 1)}
            />
          )}
        </div>
      )}
    </div>
  );
}
