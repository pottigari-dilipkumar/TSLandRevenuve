import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FileText, MapPin, TrendingUp, User } from 'lucide-react';
import { citizenApi } from '../api/citizenApi';
import { useAuthStore } from '../store/authStore';
import Alert from '../components/Alert';

const STATUS_COLORS = {
  DRAFT: 'bg-slate-100 text-slate-700',
  PENDING_APPROVAL: 'bg-yellow-100 text-yellow-800',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
};

export default function CitizenDashboardPage() {
  const { user } = useAuthStore();
  const [lands, setLands] = useState([]);
  const [registrations, setRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const [landsData, regsData] = await Promise.all([
          citizenApi.getMyLands(),
          citizenApi.getMyRegistrations(),
        ]);
        setLands(landsData);
        setRegistrations(regsData);
      } catch (err) {
        setError('Failed to load your data');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const totalEstimatedValue = lands.reduce(
    (sum, l) => sum + (l.estimatedMarketValue || 0), 0
  );

  if (loading) {
    return <div className="p-8 text-center text-slate-500">Loading your dashboard...</div>;
  }

  return (
    <div className="space-y-8">
      <Alert message={error} />

      {/* Welcome + profile completion banner */}
      {!user?.profileComplete && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 flex items-center justify-between">
          <div>
            <p className="font-medium text-amber-900">Complete your profile</p>
            <p className="text-sm text-amber-700">Add your name, mobile, and address to get the full experience.</p>
          </div>
          <Link to="/citizen/profile" className="btn-primary text-sm">Complete Profile</Link>
        </div>
      )}

      {/* Summary cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-brand-50 p-3"><MapPin className="text-brand-600" size={20} /></div>
            <div>
              <p className="text-xs text-slate-500">My Properties</p>
              <p className="text-2xl font-bold">{lands.length}</p>
            </div>
          </div>
        </div>
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-green-50 p-3"><TrendingUp className="text-green-600" size={20} /></div>
            <div>
              <p className="text-xs text-slate-500">Estimated Value</p>
              <p className="text-2xl font-bold">₹{(totalEstimatedValue / 1e7).toFixed(2)}Cr</p>
            </div>
          </div>
        </div>
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-purple-50 p-3"><FileText className="text-purple-600" size={20} /></div>
            <div>
              <p className="text-xs text-slate-500">Registrations</p>
              <p className="text-2xl font-bold">{registrations.length}</p>
            </div>
          </div>
        </div>
      </div>

      {/* My Lands */}
      <section>
        <h2 className="mb-4 text-lg font-semibold">My Land Holdings</h2>
        {lands.length === 0 ? (
          <div className="card text-center text-slate-500 py-8">No land records found linked to your Aadhaar.</div>
        ) : (
          <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                <tr>
                  <th className="px-4 py-3">Survey No.</th>
                  <th className="px-4 py-3">Location</th>
                  <th className="px-4 py-3">Area (Acres)</th>
                  <th className="px-4 py-3">Market Rate / Acre</th>
                  <th className="px-4 py-3">Estimated Value</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {lands.map((land) => (
                  <tr key={land.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-mono font-medium">{land.surveyNumber}</td>
                    <td className="px-4 py-3">{land.village}, {land.district}</td>
                    <td className="px-4 py-3">{land.areaInAcres}</td>
                    <td className="px-4 py-3">
                      {land.marketValuePerAcre > 0
                        ? `₹${Number(land.marketValuePerAcre).toLocaleString('en-IN')}`
                        : <span className="text-slate-400">N/A</span>}
                    </td>
                    <td className="px-4 py-3 font-medium text-green-700">
                      {land.estimatedMarketValue > 0
                        ? `₹${Number(land.estimatedMarketValue).toLocaleString('en-IN')}`
                        : <span className="text-slate-400">N/A</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* My Registrations */}
      <section>
        <h2 className="mb-4 text-lg font-semibold">My Registrations</h2>
        {registrations.length === 0 ? (
          <div className="card text-center text-slate-500 py-8">No registrations found where you appear as buyer, seller, or witness.</div>
        ) : (
          <div className="space-y-3">
            {registrations.map((reg) => (
              <div key={reg.id} className="card flex items-start justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-sm font-semibold">{reg.registrationRef}</span>
                    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[reg.status] || 'bg-slate-100 text-slate-700'}`}>
                      {reg.status.replace('_', ' ')}
                    </span>
                  </div>
                  <p className="mt-1 text-sm text-slate-600">
                    {reg.propertySurveyNumber} — {reg.propertyVillage}, {reg.propertyDistrict} ({reg.propertyAreaInAcres} acres)
                  </p>
                  <p className="text-xs text-slate-400 mt-1">
                    Seller: {reg.sellerName} → Buyer: {reg.buyerName}
                  </p>
                  {reg.considerationAmount && (
                    <p className="text-xs text-slate-500 mt-0.5">
                      Consideration: ₹{Number(reg.considerationAmount).toLocaleString('en-IN')}
                      {reg.stampDuty && ` · Stamp Duty: ₹${Number(reg.stampDuty).toLocaleString('en-IN')}`}
                    </p>
                  )}
                  {reg.rejectionReason && (
                    <p className="mt-1 text-xs text-red-600">Rejection: {reg.rejectionReason}</p>
                  )}
                </div>
                <div className="text-xs text-slate-400 whitespace-nowrap">
                  {new Date(reg.createdAt).toLocaleDateString('en-IN')}
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
