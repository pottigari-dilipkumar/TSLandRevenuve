import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Alert from '../components/Alert';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../store/authStore';

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
    <div className="mx-auto max-w-lg">
      <h1 className="mb-6 text-xl font-bold">Complete Your Profile</h1>
      <Alert message={error} />
      <form className="card space-y-4" onSubmit={handleSubmit}>
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
