import { Outlet } from 'react-router-dom';
import { BarChart3, MapPin, Shield, FileCheck } from 'lucide-react';

const features = [
  { icon: Shield,    label: 'Secure Aadhaar Auth',      desc: 'OTP-based citizen identity verification' },
  { icon: MapPin,    label: 'Land Record Management',   desc: 'Track and update land ownership records' },
  { icon: FileCheck, label: 'SRO Registration Workflow', desc: 'End-to-end document registration approval' },
];

export default function AuthLayout() {
  return (
    <div className="flex min-h-screen">
      {/* Left panel — hero */}
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-between bg-hero-gradient px-14 py-12">
        {/* Brand */}
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/10 backdrop-blur-sm">
            <BarChart3 size={20} className="text-white" />
          </div>
          <div>
            <p className="text-base font-bold text-white">LRMS Portal</p>
            <p className="text-xs text-indigo-300">Telangana Land Revenue</p>
          </div>
        </div>

        {/* Hero text */}
        <div>
          <h1 className="text-4xl font-extrabold leading-tight text-white">
            Land Revenue &<br />
            <span className="bg-gradient-to-r from-indigo-300 to-violet-300 bg-clip-text text-transparent">
              Registration System
            </span>
          </h1>
          <p className="mt-4 text-base text-indigo-200/80 leading-relaxed">
            Manage land records, registrations, and revenue workflows through a single secure platform.
          </p>

          {/* Feature list */}
          <div className="mt-10 space-y-4">
            {features.map(({ icon: Icon, label, desc }) => (
              <div key={label} className="flex items-start gap-4">
                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-white/10">
                  <Icon size={16} className="text-indigo-200" />
                </div>
                <div>
                  <p className="text-sm font-semibold text-white">{label}</p>
                  <p className="text-xs text-indigo-300/80">{desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Footer */}
        <p className="text-xs text-indigo-400/60">
          &copy; {new Date().getFullYear()} Government of Telangana
        </p>
      </div>

      {/* Right panel — form */}
      <div className="flex flex-1 flex-col items-center justify-center bg-surface-50 px-6 py-12">
        {/* Mobile brand */}
        <div className="mb-8 flex items-center gap-2 lg:hidden">
          <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-brand-gradient">
            <BarChart3 size={16} className="text-white" />
          </div>
          <span className="text-base font-bold text-slate-900">LRMS Portal</span>
        </div>

        <div className="w-full max-w-md animate-fade-in">
          <div className="card p-8">
            <Outlet />
          </div>
          <p className="mt-6 text-center text-xs text-slate-400">
            Secure government portal &mdash; all sessions are encrypted
          </p>
        </div>
      </div>
    </div>
  );
}
