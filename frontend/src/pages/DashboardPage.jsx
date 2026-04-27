import { useEffect, useState } from 'react';
import { BarChart, Bar, ResponsiveContainer, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
import { FileText, AlertTriangle, IndianRupee, Scale } from 'lucide-react';
import StatCard from '../components/StatCard';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

const defaultStats = {
  totalLandRecords:   1204,
  pendingMutations:   64,
  monthlyCollections: '₹ 84.7L',
  disputesOpen:       18,
};

const fallbackTrend = [
  { month: 'Jan', amount: 9.2 },
  { month: 'Feb', amount: 7.8 },
  { month: 'Mar', amount: 8.4 },
  { month: 'Apr', amount: 9.1 },
  { month: 'May', amount: 8.9 },
  { month: 'Jun', amount: 10.5 },
];

function CustomTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="rounded-xl border border-slate-200 bg-white px-3 py-2 shadow-card text-sm">
      <p className="font-semibold text-slate-700">{label}</p>
      <p className="text-brand-600">₹ {payload[0].value}L</p>
    </div>
  );
}

export default function DashboardPage() {
  const [stats, setStats] = useState(defaultStats);
  const [trend, setTrend] = useState(fallbackTrend);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([landApi.getDashboardStats(), landApi.getRevenueTrend()])
      .then(([statsRes, trendRes]) => {
        setStats({ ...defaultStats, ...statsRes });
        setTrend(Array.isArray(trendRes) && trendRes.length ? trendRes : fallbackTrend);
      })
      .catch(() => setError('Showing fallback analytics — unable to fetch live data.'));
  }, []);

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h2 className="page-title">Dashboard</h2>
          <p className="mt-1 text-sm text-slate-400">Overview of land revenue operations</p>
        </div>
      </div>

      <Alert type="info" message={error} />

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard title="Total Land Records" value={stats.totalLandRecords} subtitle="Across all districts"  icon={FileText}      accentColor="brand"   trend={4.2} />
        <StatCard title="Pending Mutations"  value={stats.pendingMutations}  subtitle="Need officer review"  icon={AlertTriangle}  accentColor="amber"   trend={-2.1} />
        <StatCard title="Monthly Collection" value={stats.monthlyCollections} subtitle="Current fiscal month" icon={IndianRupee}   accentColor="emerald" trend={8.5} />
        <StatCard title="Open Disputes"      value={stats.disputesOpen}       subtitle="Litigation in process"icon={Scale}         accentColor="rose"    trend={1.3} />
      </section>

      <section className="card">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h2 className="text-base font-semibold text-slate-900">Revenue Trend</h2>
            <p className="text-xs text-slate-400 mt-0.5">Monthly collections in ₹ Lakhs</p>
          </div>
        </div>
        <div className="h-72">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={trend} barSize={32}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
              <XAxis dataKey="month" axisLine={false} tickLine={false} tick={{ fill: '#94a3b8', fontSize: 12 }} />
              <YAxis axisLine={false} tickLine={false} tick={{ fill: '#94a3b8', fontSize: 12 }} />
              <Tooltip content={<CustomTooltip />} cursor={{ fill: '#f8fafc' }} />
              <Bar dataKey="amount" fill="url(#brandGrad)" radius={[8, 8, 0, 0]} />
              <defs>
                <linearGradient id="brandGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%"   stopColor="#6366f1" />
                  <stop offset="100%" stopColor="#8b5cf6" />
                </linearGradient>
              </defs>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </section>
    </div>
  );
}
