import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Alert from '../components/Alert';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../store/authStore';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, citizenLogin, isLoading, error, clearError } = useAuthStore();
  const [tab, setTab] = useState('staff'); // 'staff' | 'citizen'
  const [form, setForm] = useState({ username: '', password: '' });
  const [aadhaarStep, setAadhaarStep] = useState('aadhaar'); // 'aadhaar' | 'otp'
  const [aadhaar, setAadhaar] = useState('');
  const [otp, setOtp] = useState('');
  const [demoOtp, setDemoOtp] = useState('');
  const [otpLoading, setOtpLoading] = useState(false);
  const [localError, setLocalError] = useState('');

  const handleStaffLogin = async (e) => {
    e.preventDefault();
    clearError();
    try {
      await login(form);
      navigate('/dashboard');
    } catch {
      // handled in store
    }
  };

  const handleSendOtp = async (e) => {
    e.preventDefault();
    if (aadhaar.length !== 12) {
      setLocalError('Aadhaar must be 12 digits');
      return;
    }
    setLocalError('');
    setOtpLoading(true);
    try {
      const res = await authApi.citizenSendOtp(aadhaar);
      setDemoOtp(res.demoOtp);
      setAadhaarStep('otp');
    } catch (err) {
      setLocalError(err?.response?.data?.message || 'Failed to send OTP');
    } finally {
      setOtpLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    clearError();
    setLocalError('');
    try {
      const data = await citizenLogin(aadhaar, otp);
      if (!data.profileComplete) {
        navigate('/citizen/profile');
      } else {
        navigate('/citizen/dashboard');
      }
    } catch {
      // handled in store
    }
  };

  return (
    <div>
      {/* Tab switcher */}
      <div className="mb-6 flex rounded-lg border border-slate-200 overflow-hidden">
        <button
          className={`flex-1 py-2 text-sm font-medium transition ${tab === 'staff' ? 'bg-brand-600 text-white' : 'bg-white text-slate-600 hover:bg-slate-50'}`}
          onClick={() => { setTab('staff'); clearError(); setLocalError(''); }}
        >
          Staff Login
        </button>
        <button
          className={`flex-1 py-2 text-sm font-medium transition ${tab === 'citizen' ? 'bg-brand-600 text-white' : 'bg-white text-slate-600 hover:bg-slate-50'}`}
          onClick={() => { setTab('citizen'); clearError(); setLocalError(''); setAadhaarStep('aadhaar'); setDemoOtp(''); }}
        >
          Citizen (Aadhaar)
        </button>
      </div>

      {/* Staff login */}
      {tab === 'staff' && (
        <form className="space-y-4" onSubmit={handleStaffLogin}>
          <Alert message={error} />
          <div>
            <label className="mb-1 block text-sm font-medium">Username</label>
            <input
              className="input"
              type="text"
              required
              value={form.username}
              onChange={(e) => setForm((p) => ({ ...p, username: e.target.value }))}
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">Password</label>
            <input
              className="input"
              type="password"
              required
              value={form.password}
              onChange={(e) => setForm((p) => ({ ...p, password: e.target.value }))}
            />
          </div>
          <button className="btn-primary w-full" disabled={isLoading}>
            {isLoading ? 'Signing in...' : 'Sign in'}
          </button>
          <p className="text-center text-xs text-slate-400">
            Credentials: admin/admin123 · sro/sro123 · sro_asst/sro_asst123
          </p>
        </form>
      )}

      {/* Citizen Aadhaar login */}
      {tab === 'citizen' && aadhaarStep === 'aadhaar' && (
        <form className="space-y-4" onSubmit={handleSendOtp}>
          <Alert message={localError} />
          <div>
            <label className="mb-1 block text-sm font-medium">Aadhaar Number</label>
            <input
              className="input tracking-widest"
              type="text"
              maxLength={12}
              pattern="\d{12}"
              placeholder="12-digit Aadhaar number"
              required
              value={aadhaar}
              onChange={(e) => setAadhaar(e.target.value.replace(/\D/g, ''))}
            />
          </div>
          <button className="btn-primary w-full" disabled={otpLoading}>
            {otpLoading ? 'Sending OTP...' : 'Send OTP'}
          </button>
        </form>
      )}

      {tab === 'citizen' && aadhaarStep === 'otp' && (
        <form className="space-y-4" onSubmit={handleVerifyOtp}>
          <Alert message={error || localError} />
          {demoOtp && (
            <div className="rounded-lg bg-amber-50 border border-amber-200 p-3 text-sm text-amber-800">
              <strong>Demo OTP:</strong> {demoOtp}
            </div>
          )}
          <p className="text-sm text-slate-500">
            OTP sent to mobile linked with Aadhaar <strong>****{aadhaar.slice(-4)}</strong>
          </p>
          <div>
            <label className="mb-1 block text-sm font-medium">Enter OTP</label>
            <input
              className="input tracking-widest text-center text-xl"
              type="text"
              maxLength={6}
              pattern="\d{6}"
              placeholder="6-digit OTP"
              required
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
            />
          </div>
          <button className="btn-primary w-full" disabled={isLoading}>
            {isLoading ? 'Verifying...' : 'Verify & Login'}
          </button>
          <button
            type="button"
            className="w-full text-center text-sm text-brand-600"
            onClick={() => { setAadhaarStep('aadhaar'); setOtp(''); setDemoOtp(''); }}
          >
            Change Aadhaar
          </button>
        </form>
      )}
    </div>
  );
}
