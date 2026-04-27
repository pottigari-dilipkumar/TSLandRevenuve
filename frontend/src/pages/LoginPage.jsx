import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Fingerprint, KeyRound, ArrowRight, RotateCcw } from 'lucide-react';
import Alert from '../components/Alert';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';

function getHomeForRole(role) {
  if (role === ROLES.CITIZEN) return '/citizen/dashboard';
  if (role === ROLES.SRO || role === ROLES.SRO_ASSISTANT) return '/registrations';
  return '/dashboard';
}

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, citizenLogin, isLoading, error, clearError } = useAuthStore();
  const [tab, setTab] = useState('staff');
  const [form, setForm] = useState({ username: '', password: '' });
  const [aadhaarStep, setAadhaarStep] = useState('aadhaar');
  const [aadhaar, setAadhaar] = useState('');
  const [otp, setOtp] = useState('');
  const [demoOtp, setDemoOtp] = useState('');
  const [otpLoading, setOtpLoading] = useState(false);
  const [localError, setLocalError] = useState('');

  const switchTab = (t) => {
    setTab(t);
    clearError();
    setLocalError('');
    setAadhaarStep('aadhaar');
    setDemoOtp('');
    setOtp('');
  };

  const handleStaffLogin = async (e) => {
    e.preventDefault();
    clearError();
    try {
      const data = await login(form);
      navigate(getHomeForRole(data.role));
    } catch { /* handled in store */ }
  };

  const handleSendOtp = async (e) => {
    e.preventDefault();
    if (aadhaar.length !== 12) { setLocalError('Aadhaar must be 12 digits'); return; }
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
      navigate(data.profileComplete ? '/citizen/dashboard' : '/citizen/profile');
    } catch { /* handled in store */ }
  };

  return (
    <div>
      {/* Header */}
      <div className="mb-6 text-center">
        <h2 className="text-xl font-bold text-slate-900">Welcome back</h2>
        <p className="mt-1 text-sm text-slate-400">Sign in to your account to continue</p>
      </div>

      {/* Tab switcher */}
      <div className="tab-nav mb-6">
        <button
          className={tab === 'staff' ? 'tab-btn-active' : 'tab-btn'}
          onClick={() => switchTab('staff')}
        >
          <KeyRound size={13} className="inline mr-1.5 -mt-0.5" />
          Staff Login
        </button>
        <button
          className={tab === 'citizen' ? 'tab-btn-active' : 'tab-btn'}
          onClick={() => switchTab('citizen')}
        >
          <Fingerprint size={13} className="inline mr-1.5 -mt-0.5" />
          Citizen (Aadhaar)
        </button>
      </div>

      {/* Staff login */}
      {tab === 'staff' && (
        <form className="space-y-4" onSubmit={handleStaffLogin}>
          <Alert message={error} />
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Username</label>
            <input
              className="input"
              type="text"
              required
              placeholder="Enter your username"
              autoComplete="username"
              value={form.username}
              onChange={(e) => setForm((p) => ({ ...p, username: e.target.value }))}
            />
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Password</label>
            <input
              className="input"
              type="password"
              required
              placeholder="Enter your password"
              autoComplete="current-password"
              value={form.password}
              onChange={(e) => setForm((p) => ({ ...p, password: e.target.value }))}
            />
          </div>
          <button className="btn-primary mt-2 w-full" disabled={isLoading}>
            {isLoading ? 'Signing in…' : (
              <>Sign in <ArrowRight size={15} /></>
            )}
          </button>
          <div className="rounded-xl bg-slate-50 border border-slate-200 px-4 py-3 text-xs text-slate-400 space-y-1">
            <p className="font-medium text-slate-500">Demo credentials</p>
            <p>admin / admin123 &nbsp;·&nbsp; sro / sro123 &nbsp;·&nbsp; sro_asst / sro_asst123</p>
          </div>
        </form>
      )}

      {/* Citizen — step 1: Aadhaar */}
      {tab === 'citizen' && aadhaarStep === 'aadhaar' && (
        <form className="space-y-4" onSubmit={handleSendOtp}>
          <Alert message={localError} />
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Aadhaar Number</label>
            <input
              className="input tracking-widest font-mono"
              type="text"
              inputMode="numeric"
              maxLength={12}
              placeholder="12-digit Aadhaar number"
              required
              value={aadhaar}
              onChange={(e) => setAadhaar(e.target.value.replace(/\D/g, ''))}
            />
            <p className="mt-1.5 text-xs text-slate-400">Your Aadhaar number is used only for identity verification</p>
          </div>
          <button className="btn-primary mt-2 w-full" disabled={otpLoading}>
            {otpLoading ? 'Sending OTP…' : (<>Send OTP <ArrowRight size={15} /></>)}
          </button>
        </form>
      )}

      {/* Citizen — step 2: OTP */}
      {tab === 'citizen' && aadhaarStep === 'otp' && (
        <form className="space-y-4" onSubmit={handleVerifyOtp}>
          <Alert message={error || localError} />

          {demoOtp && (
            <div className="flex items-center gap-3 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3">
              <span className="text-lg">🔑</span>
              <div>
                <p className="text-xs font-semibold text-amber-700">Demo OTP (dev only)</p>
                <p className="text-xl font-bold tracking-widest text-amber-800">{demoOtp}</p>
              </div>
            </div>
          )}

          <div>
            <p className="mb-3 text-sm text-slate-500">
              OTP sent to mobile linked with Aadhaar ending in <span className="font-semibold text-slate-700">****{aadhaar.slice(-4)}</span>
            </p>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Enter OTP</label>
            <input
              className="input text-center text-2xl font-bold tracking-[0.5em]"
              type="text"
              inputMode="numeric"
              maxLength={6}
              placeholder="——————"
              required
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
            />
          </div>

          <button className="btn-primary mt-2 w-full" disabled={isLoading}>
            {isLoading ? 'Verifying…' : (<>Verify &amp; Sign In <ArrowRight size={15} /></>)}
          </button>

          <button
            type="button"
            onClick={() => { setAadhaarStep('aadhaar'); setOtp(''); setDemoOtp(''); }}
            className="btn-ghost w-full text-xs"
          >
            <RotateCcw size={13} /> Change Aadhaar number
          </button>
        </form>
      )}
    </div>
  );
}
