import { useEffect, useState } from 'react';
import { BarChart, Bar, ResponsiveContainer, XAxis, YAxis, Tooltip } from 'recharts';
import StatCard from '../components/StatCard';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

const defaultStats = {
  totalLandRecords: 1204,
  pendingMutations: 64,
  monthlyCollections: '₹ 84.7L',
  disputesOpen: 18,
};

const fallbackTrend = [
  { month: 'Jan', amount: 9.2 },
  { month: 'Feb', amount: 7.8 },
  { month: 'Mar', amount: 8.4 },
  { month: 'Apr', amount: 9.1 },
  { month: 'May', amount: 8.9 },
  { month: 'Jun', amount: 10.5 },
];

export default function DashboardPage() {
  const [stats, setStats] = useState(defaultStats);
  const [trend, setTrend] = useState(fallbackTrend);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const [statsRes, trendRes] = await Promise.all([landApi.getDashboardStats(), landApi.getRevenueTrend()]);
        setStats({ ...defaultStats, ...statsRes });
        setTrend(Array.isArray(trendRes) && trendRes.length ? trendRes : fallbackTrend);
      } catch {
        setError('Showing fallback analytics. Unable to fetch latest dashboard data.');
      }
    };

    load();
  }, []);

  return (
    <div className="space-y-6">
      <Alert type="info" message={error} />
      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard title="Total Land Records" value={stats.totalLandRecords} subtitle="Across all districts" />
        <StatCard title="Pending Mutations" value={stats.pendingMutations} subtitle="Need officer review" />
        <StatCard title="Monthly Collection" value={stats.monthlyCollections} subtitle="Current fiscal month" />
        <StatCard title="Open Disputes" value={stats.disputesOpen} subtitle="Litigation in process" />
      </section>

      <section className="card">
        <h2 className="text-lg font-semibold">Revenue Trend (₹ Lakhs)</h2>
        <div className="mt-4 h-80">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={trend}>
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="amount" fill="#2563eb" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </section>
    </div>
  );
}
