// Copyright (c) 2026 DivaTech. All rights reserved.
// Proprietary and confidential. Unauthorized copying or distribution is strictly prohibited.
// Website: https://www.divatech.in | Contact: legal@divatech.in

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck } from 'lucide-react';
import Alert from '../components/Alert';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../store/authStore';

function maskAadhaar(aadhaar) {
  if (!aadhaar || aadhaar.length < 4) return aadhaar || '—';
  return 'XXXX XXXX ' + aadhaar.slice(-4);
}

export default function CitizenProfilePage() {
  const navigate = useNavigate();
  const { user, updateCitizenProfile } = useAuthStore();
  const [form, setForm] = useState({
    fullName: user?.fullName || '',
    mobile: user?.mobile || '',
    email: user?.email || '',
    address: user?.address || '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await authApi.citizenUpdateProfile(form);
      updateCitizenProfile({
        fullName: data.fullName,
        mobile: data.mobile,
        email: data.email,
        profileComplete: true,
      });
      navigate('/citizen/dashboard');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg space-y-5">
      <h1 className="text-xl font-bold">My Profile</h1>

      {/* Aadhaar Identity Card */}
      <div className="rounded-2xl border border-indigo-200 bg-gradient-to-br from-indigo-50 to-blue-50 p-5">
        <div className="flex items-center gap-3 mb-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-600 text-white shadow">
            <ShieldCheck size={20} />
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-indigo-500">Aadhaar Identity</p>
            <p className="text-sm text-slate-500">Government of India · Unique Identification</p>
          </div>
        </div>
        <p className="font-mono text-2xl font-bold tracking-[0.2em] text-indigo-900">
          {maskAadhaar(user?.aadhaarNumber)}
        </p>
        <p className="mt-1 text-xs text-indigo-400">
          This is your verified identity. It cannot be changed here.
        </p>
      </div>

      <Alert message={error} />
      <form className="card space-y-4" onSubmit={handleSubmit}>
        <p className="text-sm font-semibold text-slate-700">Contact Details</p>
        <div>
          <label className="mb-1 block text-sm font-medium">Full Name *</label>
          <input
            className="input"
            required
            value={form.fullName}
            onChange={(e) => setForm((p) => ({ ...p, fullName: e.target.value }))}
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Mobile Number *</label>
          <input
            className="input"
            type="tel"
            pattern="\d{10}"
            maxLength={10}
            required
            placeholder="10-digit mobile"
            value={form.mobile}
            onChange={(e) => setForm((p) => ({ ...p, mobile: e.target.value.replace(/\D/g, '') }))}
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Email (optional)</label>
          <input
            className="input"
            type="email"
            value={form.email}
            onChange={(e) => setForm((p) => ({ ...p, email: e.target.value }))}
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Address *</label>
          <textarea
            className="input"
            rows={3}
            required
            value={form.address}
            onChange={(e) => setForm((p) => ({ ...p, address: e.target.value }))}
          />
        </div>
        <button className="btn-primary w-full" disabled={loading}>
          {loading ? 'Saving...' : 'Save Profile'}
        </button>
      </form>
    </div>
  );
}
